package de.tschumacher.mqttservice.message;

public class MQTTMessage<T> {
  private T content;
  private final MQTTMessageCoder<T> coder;

  public MQTTMessage(final MQTTMessageCoder<T> coder, final byte[] content) {
    super();
    this.coder = coder;
    this.content = this.coder.encode(content);
  }

  public MQTTMessage(final MQTTMessageCoder<T> coder, final T content) {
    super();
    this.coder = coder;
    this.content = content;
  }

  public T getContent() {
    return this.content;
  }

  public byte[] getByteContent() {
    return this.coder.decode(this.content);
  }

  public void setContent(final T content) {
    this.content = content;
  }

  public void setContent(final byte[] content) {
    this.content = this.coder.encode(content);
  }

}
