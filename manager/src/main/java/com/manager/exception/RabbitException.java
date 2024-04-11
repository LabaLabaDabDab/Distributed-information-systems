package com.manager.exception;

public class RabbitException extends Exception{
    public RabbitException(){}

    public RabbitException(String errorMessage){
        super(errorMessage);
    }

    public RabbitException(Throwable cause) {
        super (cause);
    }

    public RabbitException(String message, Throwable cause) {
        super (message, cause);
    }

}
