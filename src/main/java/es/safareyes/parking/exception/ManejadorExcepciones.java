package es.safareyes.parking.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

@RestControllerAdvice
public class ManejadorExcepciones extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail gestionarRecursoNoEncontrado(RecursoNoEncontradoException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ProblemDetail gestionarReglaNegocio(ReglaNegocioException excepcion) {
        return ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(422), excepcion.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException excepcion, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        ProblemDetail problema = excepcion.getBody();
        problema.setProperty("campos", excepcion.getFieldErrors().stream()
                .map(error -> error.getField())
                .distinct()
                .toList());
        return handleExceptionInternal(excepcion, problema, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException excepcion, HttpHeaders headers,
            HttpStatusCode status, WebRequest request
    ) {
        ProblemDetail problema = excepcion.getBody();
        problema.setProperty("campos", excepcion.getParameterValidationResults().stream()
                .map(resultado -> resultado.getMethodParameter().getParameterName())
                .filter(Objects::nonNull)
                .distinct()
                .toList());
        return handleExceptionInternal(excepcion, problema, headers, status, request);
    }
}
