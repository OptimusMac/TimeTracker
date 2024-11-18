package ru.optimusmac.timetracker.response;

public class UploadResponse {
  private String userId;
  private boolean success;

  public UploadResponse(String userId, boolean success) {
    this.userId = userId;
    this.success = success;
  }

  public String getUserId() {
    return userId;
  }

  public boolean isSuccess() {
    return success;
  }
}
