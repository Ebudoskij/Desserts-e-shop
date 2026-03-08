package com.ebudoskij.dessert_shop.service.impl;

import com.ebudoskij.dessert_shop.exception.EntityNotFoundException;
import com.ebudoskij.dessert_shop.mapper.ProductMapper;
import com.ebudoskij.dessert_shop.model.Category;
import com.ebudoskij.dessert_shop.model.Product;
import com.ebudoskij.dessert_shop.model.dto.PageResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductCreateDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductResponseDto;
import com.ebudoskij.dessert_shop.model.dto.product.ProductUpdateDto;
import com.ebudoskij.dessert_shop.model.dto.media.MediaResponseDto;
import com.ebudoskij.dessert_shop.repository.ProductRepository;
import com.ebudoskij.dessert_shop.service.CategoryService;
import com.ebudoskij.dessert_shop.service.MediaService;
import com.ebudoskij.dessert_shop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryService categoryService;
    private final MediaService mediaService;

    @Override
    public PageResponseDto<ProductResponseDto> getAll(int page, int size, String sortBy, String sortDir, String searchQuery) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        Specification<Product> spec = (root, query, criteriaBuilder) -> {
            Specification<Product> notDeletedSpec = (r, q, cb) -> cb.isFalse(r.get("isDeleted"));

            if (searchQuery == null || searchQuery.trim().isEmpty()) {
                return notDeletedSpec.toPredicate(root, query, criteriaBuilder);
            }

            String pattern = "%" + searchQuery.toLowerCase() + "%";
            Specification<Product> searchSpec = (r, q, cb) -> cb.or(
                    cb.like(cb.lower(r.get("name")), pattern),
                    cb.like(cb.lower(r.get("description")), pattern)
            );

            return criteriaBuilder.and(
                    notDeletedSpec.toPredicate(root, query, criteriaBuilder),
                    searchSpec.toPredicate(root, query, criteriaBuilder)
            );
        };

        Page<Product> productPage = productRepository.findAll(spec, pageRequest);
        
        PageResponseDto<ProductResponseDto> response = new PageResponseDto<>();
        response.setContent(productPage.getContent().stream()
                .map(productMapper::toDto)
                .toList());
        response.setPageNo(productPage.getNumber());
        response.setPageSize(productPage.getSize());
        response.setTotalElements(productPage.getTotalElements());
        response.setTotalPages(productPage.getTotalPages());
        response.setLast(productPage.isLast());

        return response;
    }

    @Override
    public ProductResponseDto getById(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        ProductResponseDto responseDto = productMapper.toDto(product);

        List<MediaResponseDto> mediaDtos = mediaService.getEntityImages("Product", id).stream()
                .map(m -> {
                    MediaResponseDto mediaDto = new MediaResponseDto();
                    mediaDto.setId(m.getId());
                    mediaDto.setUrl(m.getUrl());
                    return mediaDto;
                })
                .toList();
        responseDto.setImageUrls(mediaDtos);

        return responseDto;
    }

    @Override
    public void updateById(Long id, ProductUpdateDto dto) {
        Product existingProduct = productRepository.findById(id)
                        .filter(p -> !p.getIsDeleted())
                        .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        productMapper.updateEntityFromDto(dto, existingProduct);

        if (dto.getCategoryId() != null) {
            Category category = categoryService.getById(dto.getCategoryId());
            existingProduct.setCategory(category);
        } else {
            existingProduct.setCategory(null);
        }

        Product savedProduct = productRepository.save(existingProduct);
        mediaService.deleteEntityImages(dto.getDeletedImageIds());
        mediaService.saveEntityImages("Product", savedProduct.getId(), dto.getImages());
    }

    @Override
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        product.setIsDeleted(true);
        productRepository.save(product);
    }

    @Override
    public void createProduct(ProductCreateDto dto) {
        Product product = productMapper.toEntity(dto);
        product.setIsDeleted(false);

        if (dto.getCategoryId() != null) {
            Category category = categoryService.getById(dto.getCategoryId());
            product.setCategory(category);
        }

        Product savedProduct = productRepository.save(product);
        mediaService.saveEntityImages("Product", savedProduct.getId(), dto.getImages());
    }

    @Override
    public ProductResponseDto getToUpdate(Long id) {
        Product product = productRepository.findById(id)
                .filter(p -> !p.getIsDeleted())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

        ProductResponseDto responseDto = productMapper.toDto(product);
        
        List<MediaResponseDto> mediaDtos = mediaService.getEntityImages("Product", id).stream()
                .map(m -> {
                    MediaResponseDto mediaDto = new MediaResponseDto();
                    mediaDto.setId(m.getId());
                    mediaDto.setUrl(m.getUrl());
                    return mediaDto;
                })
                .toList();
        responseDto.setImageUrls(mediaDtos);

        return responseDto;
    }
}
