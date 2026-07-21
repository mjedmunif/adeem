package org.example.adeem.ControllerAdvice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.example.adeem.API.APIException;
import org.example.adeem.API.APIResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ==================== 400 - أخطاء منطق العمل (اللي ترميها انت يدوياً) ====================
    @ExceptionHandler(APIException.class)
    public ResponseEntity<?> handleApiException(APIException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse(ex.getMessage()));

    }


    // ==================== 400 - أخطاء Validation (@Valid على DTOs) ====================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        String combinedMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(" | "));
        log.warn("Error: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new APIResponse(combinedMessage));
    }



    // ==================== 403 - صلاحيات (Spring Security) ====================
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new APIResponse("ليس لديك صلاحية للوصول لهذا المورد"));
    }

    // ==================== 409 - تعارض بقاعدة البيانات (إيميل مكرر، unique constraint) ====================
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        // نسجل التفاصيل التقنية الكاملة بالـ log فقط (اسم القيد، الجدول، إلخ)

        // ما نرجع ex.getMessage() للمستخدم أبداً — يكشف تفاصيل قاعدة البيانات
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new APIResponse("البيانات المدخلة مكررة أو تتعارض مع بيانات موجودة"));
    }

    // ==================== 503 - مشكلة اتصال عامة بقاعدة البيانات ====================
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<?> handleDataAccessException(DataAccessException ex) {
        // مشكلة اتصال أو استعلام فشل بسبب السيرفر نفسه، مو خطأ المستخدم

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new APIResponse("حدث خطأ بالاتصال بقاعدة البيانات، حاول لاحقاً"));
    }

    // ==================== 500 - أي خطأ غير متوقع (Fallback) ====================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception ex) {
        // نسجل الخطأ كامل بالـ log فقط، أبداً لا نكشف ex.getMessage() للمستخدم
        log.info(ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new APIResponse("حدث خطأ غير متوقع، حاول لاحقاً"));
    }
}
