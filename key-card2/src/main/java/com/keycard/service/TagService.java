package com.keycard.service;

import com.keycard.dto.TagDTO;
import com.keycard.dto.TagRequest;
import com.keycard.entity.Tag;
import com.keycard.repository.TagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<TagDTO> listAll() {
        return tagRepository.findAllByOrderByNameAsc().stream()
                .map(t -> new TagDTO(t.getId(), t.getName(), tagRepository.countCardsByTagId(t.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public TagDTO create(TagRequest request) {
        String name = request.getName().trim();
        if (tagRepository.existsByName(name)) {
            throw new RuntimeException("标签已存在");
        }
        Tag tag = new Tag();
        tag.setName(name);
        tagRepository.save(tag);
        return new TagDTO(tag.getId(), tag.getName(), 0);
    }

    @Transactional
    public TagDTO rename(Long id, TagRequest request) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("标签不存在"));
        String newName = request.getName().trim();
        if (tagRepository.existsByNameAndIdNot(newName, id)) {
            throw new RuntimeException("标签名已存在");
        }
        tag.setName(newName);
        tagRepository.save(tag);
        return new TagDTO(tag.getId(), tag.getName(), tagRepository.countCardsByTagId(tag.getId()));
    }

    @Transactional
    public void delete(Long id) {
        tagRepository.deleteById(id);
    }
}
