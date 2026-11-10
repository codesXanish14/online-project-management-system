package com.oms.core.constants;

public enum OrderStatus {
    PENDING,            // Placed by user, awaiting admin assignment
    ASSIGNED,           // Assigned to a delivery partner by admin
    OUT_FOR_DELIVERY,   // Picked up by the delivery partner
    PLACED,             // Cash Collected & Delivered successfully (History Archive)
    CANCELLED           // Aborted by user or admin
}