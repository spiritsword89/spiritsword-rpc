package com.spiritsword.exceptions;

public class RequestedClassNotDeterminedException extends RuntimeException{
    public RequestedClassNotDeterminedException(){
        super("The requested method is found in two or more implementations");
    }
}
