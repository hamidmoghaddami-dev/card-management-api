package com.isc.cardManagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponseDto<T> {

    private List<T> content;           // محتویات صفحه فعلی
    private int pageNumber;            // شماره صفحه فعلی (از 0 شروع می‌شود)
    private int pageSize;              // تعداد آیتم‌های در هر صفحه
    private long totalElements;        // تعداد کل عناصر
    private int totalPages;            // تعداد کل صفحات
    private boolean first;             // آیا اولین صفحه است؟
    private boolean last;              // آیا آخرین صفحه است؟
    private boolean empty;             // آیا صفحه خالی است؟
}
