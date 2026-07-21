package org.example.adeem.Enums;

public enum PaymentStatus {
    PENDING,    // تم إنشاء سجل الدفع، بانتظار إتمام الدفع فعلياً
    PAID,       // الدفع تم بنجاح
    FAILED,     // الدفع فشل (رفض البطاقة، خطأ من البوابة، إلخ)
    REFUNDED
}