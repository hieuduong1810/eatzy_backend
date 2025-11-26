package com.example.FoodDelivery.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.FoodDelivery.domain.OrderItemOption;
import com.example.FoodDelivery.domain.MenuOption;
import com.example.FoodDelivery.domain.OrderItem;
import com.example.FoodDelivery.domain.res.ResultPaginationDTO;
import com.example.FoodDelivery.domain.res.order.ResOrderItemOptionDTO;
import com.example.FoodDelivery.repository.MenuOptionRepository;
import com.example.FoodDelivery.repository.OrderItemOptionRepository;
import com.example.FoodDelivery.repository.OrderItemRepository;
import com.example.FoodDelivery.util.error.IdInvalidException;

import java.util.stream.Collectors;

@Service
public class OrderItemOptionService {
    private final OrderItemOptionRepository orderItemOptionRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuOptionRepository menuOptionRepository;

    public OrderItemOptionService(OrderItemOptionRepository orderItemOptionRepository,
            OrderItemRepository orderItemRepository,
            MenuOptionRepository menuOptionRepository) {
        this.orderItemOptionRepository = orderItemOptionRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuOptionRepository = menuOptionRepository;
    }

    private ResOrderItemOptionDTO convertToResOrderItemOptionDTO(OrderItemOption orderItemOption) {
        ResOrderItemOptionDTO dto = new ResOrderItemOptionDTO();
        dto.setId(orderItemOption.getId());

        // Convert menu option
        if (orderItemOption.getMenuOption() != null) {
            ResOrderItemOptionDTO.MenuOption menuOption = new ResOrderItemOptionDTO.MenuOption();
            menuOption.setId(orderItemOption.getMenuOption().getId());
            menuOption.setName(orderItemOption.getMenuOption().getName());
            menuOption.setPriceAdjustment(orderItemOption.getMenuOption().getPriceAdjustment());
            dto.setMenuOption(menuOption);
        }

        return dto;
    }

    public OrderItemOption getOrderItemOptionById(Long id) {
        Optional<OrderItemOption> orderItemOptionOpt = this.orderItemOptionRepository.findById(id);
        return orderItemOptionOpt.orElse(null);
    }

    public ResOrderItemOptionDTO getOrderItemOptionDTOById(Long id) {
        OrderItemOption orderItemOption = getOrderItemOptionById(id);
        if (orderItemOption == null) {
            return null;
        }
        return convertToResOrderItemOptionDTO(orderItemOption);
    }

    public List<OrderItemOption> getOrderItemOptionsByOrderItemId(Long orderItemId) {
        return this.orderItemOptionRepository.findByOrderItemId(orderItemId);
    }

    public OrderItemOption createOrderItemOption(OrderItemOption orderItemOption) throws IdInvalidException {
        // check menu option exists
        if (orderItemOption.getMenuOption() != null) {
            MenuOption menuOption = this.menuOptionRepository.findById(orderItemOption.getMenuOption().getId())
                    .orElse(null);
            if (menuOption == null) {
                throw new IdInvalidException(
                        "Menu option not found with id: " + orderItemOption.getMenuOption().getId());
            }
            orderItemOption.setMenuOption(menuOption);
            orderItemOption.setOptionName(menuOption.getName());
            orderItemOption.setPriceAtPurchase(menuOption.getPriceAdjustment());
        } else {
            throw new IdInvalidException("Menu option is required");
        }

        // check order item exists
        if (orderItemOption.getOrderItem() != null) {
            OrderItem orderItem = this.orderItemRepository.findById(orderItemOption.getOrderItem().getId())
                    .orElse(null);
            if (orderItem == null) {
                throw new IdInvalidException("Order item not found with id: " + orderItemOption.getOrderItem().getId());
            }
            orderItemOption.setOrderItem(orderItem);
        } else {
            throw new IdInvalidException("Order item is required");
        }

        return orderItemOptionRepository.save(orderItemOption);
    }

    public ResOrderItemOptionDTO createOrderItemOptionDTO(OrderItemOption orderItemOption) throws IdInvalidException {
        OrderItemOption savedOrderItemOption = createOrderItemOption(orderItemOption);
        return convertToResOrderItemOptionDTO(savedOrderItemOption);
    }

    public OrderItemOption updateOrderItemOption(OrderItemOption orderItemOption) throws IdInvalidException {
        // check id
        OrderItemOption currentOrderItemOption = getOrderItemOptionById(orderItemOption.getId());
        if (currentOrderItemOption == null) {
            throw new IdInvalidException("Order item option not found with id: " + orderItemOption.getId());
        }

        if (orderItemOption.getOptionName() != null) {
            currentOrderItemOption.setOptionName(orderItemOption.getOptionName());
        }
        if (orderItemOption.getPriceAtPurchase() != null) {
            currentOrderItemOption.setPriceAtPurchase(orderItemOption.getPriceAtPurchase());
        }

        if (orderItemOption.getMenuOption() != null) {
            MenuOption menuOption = this.menuOptionRepository.findById(orderItemOption.getMenuOption().getId())
                    .orElse(null);
            if (menuOption == null) {
                throw new IdInvalidException(
                        "Menu option not found with id: " + orderItemOption.getMenuOption().getId());
            }
            currentOrderItemOption.setMenuOption(menuOption);
            currentOrderItemOption.setOptionName(menuOption.getName());
            currentOrderItemOption.setPriceAtPurchase(menuOption.getPriceAdjustment());
        }

        if (orderItemOption.getOrderItem() != null) {
            OrderItem orderItem = this.orderItemRepository.findById(orderItemOption.getOrderItem().getId())
                    .orElse(null);
            if (orderItem == null) {
                throw new IdInvalidException("Order item not found with id: " + orderItemOption.getOrderItem().getId());
            }
            currentOrderItemOption.setOrderItem(orderItem);
        }

        return orderItemOptionRepository.save(currentOrderItemOption);
    }

    public ResOrderItemOptionDTO updateOrderItemOptionDTO(OrderItemOption orderItemOption) throws IdInvalidException {
        OrderItemOption updatedOrderItemOption = updateOrderItemOption(orderItemOption);
        return convertToResOrderItemOptionDTO(updatedOrderItemOption);
    }

    public ResultPaginationDTO getAllOrderItemOptions(Specification<OrderItemOption> spec, Pageable pageable) {
        Page<OrderItemOption> page = this.orderItemOptionRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent());
        return result;
    }

    public ResultPaginationDTO getAllOrderItemOptionsDTO(Specification<OrderItemOption> spec, Pageable pageable) {
        Page<OrderItemOption> page = this.orderItemOptionRepository.findAll(spec, pageable);
        ResultPaginationDTO result = new ResultPaginationDTO();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(page.getTotalElements());
        meta.setPages(page.getTotalPages());
        result.setMeta(meta);
        result.setResult(page.getContent().stream()
                .map(this::convertToResOrderItemOptionDTO)
                .collect(Collectors.toList()));
        return result;
    }

    public void deleteOrderItemOption(Long id) {
        this.orderItemOptionRepository.deleteById(id);
    }
}
