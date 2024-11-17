package ru.optimusmac.response;

public class ErrorResponse {
  private String message;
  private String field;

  public ErrorResponse(String message, String field) {
    this.message = message;
    this.field = field;
  }

  public String getMessage() {
    return message;
  }

  public String getField() {
    return field;
  }
}
