package hr.fer.zemris.srsv;

import java.io.Serializable;

public class SEMMessage implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean isGreen;

	public SEMMessage(boolean isGreen) {
		super();
		this.isGreen = isGreen;
	}

	public boolean isGreen() { return isGreen; }

	public void setGreen(boolean isGreen) { this.isGreen = isGreen; }

}
