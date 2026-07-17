package com.lcwd.electronicStore.ElectronicStore.exceptions;

/*
Purpose:
Signals invalid client requests that should return a clear 400 response.
*/
import lombok.Builder;

@Builder
public class BadApiRequestException extends RuntimeException {
 public BadApiRequestException(){
     super(("Bad Api request!!!"));
 }
 public BadApiRequestException(String message){
     super(message);
 }
}
