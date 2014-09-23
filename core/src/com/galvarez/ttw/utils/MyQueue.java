package com.galvarez.ttw.utils;


public class MyQueue<T> {
	
	private int size;
	private Node head;
	private Node tail;
	
	public MyQueue() {
		head = tail = null;
		size = 0;
	}
	
	public void push(T data) {
		Node node = new Node();
		node.data = data;
		node.behind = null;
		if (size > 0) {
			tail.behind = node;
			tail = node;
			size++;
		}
		else {
			head = node;
			tail = node;
			size = 1;
		}
	}
	
	public T poll() {
		if (size > 0) { 
			T ret = head.data;
			head = head.behind;
			size--;
			return ret;
		}
		return null;
	}
	
	public int size() {
		return size;
	}
	
	public boolean contains(T data, boolean identity) {
		Node current = head;
		
		if (identity || data==null) {
			while (current != null) {
				if (current.data == data) return true;
				current = current.behind;
			}
			return false;
		}
		
		while (current != null) {
			if (current.data.equals(data)) return true;
			current = current.behind;
		}
		return false;
	}
	
	public void remove(T data, boolean identity) {
		Node current = head;
		if (current == null || data == null) return;
		
		if (identity) {
			if (head.data == data) {
				head = head.behind;
				if (head == null) tail = null;
				size--;
				return;
			}
			
			while (current.behind != null) {
				if (current.behind.data == data) {
					current.behind = current.behind.behind;
					if (current.behind == null) tail = current;
					size--;
					return;
				}
				current = current.behind;
			}
			return;
		}
		
		if (head.data.equals(data)) {
			head = head.behind;
			if (head == null) tail = null;
			size--;
			return;
		}
		
		while (current.behind != null) {
			if (current.behind.data.equals(data)) {
				current.behind = current.behind.behind;
				if (current.behind == null) tail = current;
				size--;
				return;
			}
			current = current.behind;
		}
	}
	
	public void clear() {
		head = tail = null;
		size = 0;
	}
	
	@Override
  public String toString() {
		String ret = "" + size;
		Node current = head;
		while (current != null) {
			ret += "\t" + current.data.toString();
			current = current.behind;
		}
		return ret;
	}
	
	private class Node {
		public Node behind;
		public T data;
	}
}
