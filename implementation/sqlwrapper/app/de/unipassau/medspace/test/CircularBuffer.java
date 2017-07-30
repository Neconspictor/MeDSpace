package de.unipassau.medspace.test;

/**
 * Created by David Goeth on 30.07.2017.
 */
public class CircularBuffer {
  private int maxSize;
  private int front = 0;
  private int rear = 0;
  private int bufLen = 0;
  private char[] buf;


  public CircularBuffer(int size) {
    maxSize = size;
    front = rear = 0;
    rear = 0;
    bufLen = 0;
    buf = new char[maxSize];
  }

  /**
   * provides size of buffer
   **/
  public int getSize() {
    return bufLen;
  }

  /**
   * function to clear buffer
   **/
  public void clear() {
    front = rear = 0;
    rear = 0;
    bufLen = 0;
    buf = new char[maxSize];
  }

  /**
   * checks if buffer is empty
   **/
  public boolean isEmpty() {
    return bufLen == 0;
  }

  /**
   * checks if buffer is full
   **/
  public boolean isFull() {
    return bufLen == maxSize;
  }

  /**
   * inserts an element
   **/
  public void insert(char c) {
    if (!isFull()) {
      bufLen++;
      rear = (rear + 1) % maxSize;
      buf[rear] = c;
    } else {
      System.out.println("Error : Underflow Exception");
      throw new IndexOutOfBoundsException("Buffer overflow");
    }
  }

  /**
   * deletes an element
   **/
  public char delete() {
    if (!isEmpty()) {
      bufLen--;
      front = (front + 1) % maxSize;
      return buf[front];
    } else {
      throw new IndexOutOfBoundsException("Buffer underflow");
    }
  }
}