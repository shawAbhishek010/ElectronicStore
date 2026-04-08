package com.lcwd.electronicStore.ElectronicStore.exceptions;

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
