package hr.fer.zemris.srsv;

import java.io.Serializable;

public class UPRMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private State currentState;

	public UPRMessage(State currentState) {
		super();
		this.currentState = currentState;
	}

	public State getCurrentState() { return currentState; }

	public void setCurrentState(State currentState) { this.currentState = currentState; }

	@Override
	public String toString() {
		return "UPRMessage [currentState=" + currentState + "]";
	}

}
