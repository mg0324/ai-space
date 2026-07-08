package com.keycard.service;

import com.keycard.dto.CardDTO;
import com.keycard.dto.CardRequest;
import com.keycard.dto.TagInfo;
import com.keycard.entity.Card;
import com.keycard.entity.Tag;
import com.keycard.repository.CardRepository;
import com.keycard.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final TagRepository tagRepository;

    public CardService(CardRepository cardRepository, TagRepository tagRepository) {
        this.cardRepository = cardRepository;
        this.tagRepository = tagRepository;
    }

    public List<CardDTO> listAll() {
        return cardRepository.findAllByOrderByUpdatedAtDesc().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CardDTO> findByTag(String tagName) {
        return cardRepository.findByTagName(tagName).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<CardDTO> search(String keyword) {
        return cardRepository.search(keyword).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CardDTO getById(Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("卡片不存在"));
        return toDTO(card);
    }

    @Transactional
    public CardDTO create(CardRequest request) {
        Card card = new Card();
        card.setTitle(request.getTitle().trim());
        card.setContent(request.getContent().trim());
        card.setSource(request.getSource() != null ? request.getSource().trim() : "");
        card.setTags(resolveTags(request.getTagIds()));
        cardRepository.save(card);
        return toDTO(card);
    }

    @Transactional
    public CardDTO update(Long id, CardRequest request) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("卡片不存在"));
        card.setTitle(request.getTitle().trim());
        card.setContent(request.getContent().trim());
        card.setSource(request.getSource() != null ? request.getSource().trim() : "");
        if (request.getTagIds() != null) {
            card.setTags(resolveTags(request.getTagIds()));
        }
        cardRepository.save(card);
        return toDTO(card);
    }

    @Transactional
    public void delete(Long id) {
        cardRepository.deleteById(id);
    }

    public List<CardDTO> exportAll() {
        return listAll();
    }

    @Transactional
    public int importCards(List<CardDTO> data) {
        int count = 0;
        for (CardDTO item : data) {
            if (item.getTitle() == null || item.getContent() == null) continue;
            Card card = new Card();
            card.setTitle(item.getTitle().trim());
            card.setContent(item.getContent().trim());
            card.setSource(item.getSource() != null ? item.getSource().trim() : "");

            Set<Tag> tags = new HashSet<>();
            if (item.getTags() != null) {
                for (TagInfo tagInfo : item.getTags()) {
                    String tagName = tagInfo.getName();
                    if (tagName == null || tagName.isBlank()) continue;
                    Tag tag = tagRepository.findByName(tagName.trim())
                            .orElseGet(() -> {
                                Tag t = new Tag();
                                t.setName(tagName.trim());
                                t.setColor(tagInfo.getColor());
                                return tagRepository.save(t);
                            });
                    tags.add(tag);
                }
            }
            card.setTags(tags);
            cardRepository.save(card);
            count++;
        }
        return count;
    }

    public List<Card> findCardsByIds(List<Long> ids) {
        List<Card> cards = cardRepository.findAllById(ids);
        Map<Long, Card> map = cards.stream().collect(Collectors.toMap(Card::getId, c -> c));
        List<Card> ordered = new ArrayList<>();
        for (Long id : ids) {
            Card c = map.get(id);
            if (c != null) ordered.add(c);
        }
        return ordered;
    }

    private Set<Tag> resolveTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) return new HashSet<>();
        return new HashSet<>(tagRepository.findAllById(tagIds));
    }

    private CardDTO toDTO(Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setTitle(card.getTitle());
        dto.setContent(card.getContent());
        dto.setSource(card.getSource());
        dto.setTags(card.getTags().stream()
                .map(t -> new TagInfo(t.getName(), t.getColor()))
                .sorted(Comparator.comparing(TagInfo::getName))
                .collect(Collectors.toList()));
        dto.setCreatedAt(card.getCreatedAt());
        dto.setUpdatedAt(card.getUpdatedAt());
        return dto;
    }
}
