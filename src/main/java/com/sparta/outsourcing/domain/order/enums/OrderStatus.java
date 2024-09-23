package com.sparta.outsourcing.domain.order.enums;

// 주문 요청은 고객만 가능하고 상태변경은 사장님만 가능하며 주문 취소는 고객, 사장님 둘 다 가능합니다
public enum OrderStatus {
    ORDER_PLACED,       // 주문 접수 (고객, 사장님 취소가능)
    ORDER_CONFIRMED,    // 주문 확인 (고객 , 사장님 취소가능)
    ORDER_PREPARING,    // 주문 준비 중 (사장님만 취소가능)
    ORDER_ON_THE_WAY,   // 배달 중 (취소불가)
    ORDER_DELIVERED,    // 배달 완료 (취소불가)
    ORDER_CANCELED      // 주문 취소 (접수, 확인, 준비중(사장님만) 상태에서만 가능)
}