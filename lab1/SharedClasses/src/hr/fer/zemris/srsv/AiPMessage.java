package hr.fer.zemris.srsv;

import java.io.Serializable;

import hr.fer.zemris.srsv.AiPEnums.AiPDirection;
import hr.fer.zemris.srsv.AiPEnums.AiPPosition;
import hr.fer.zemris.srsv.AiPEnums.AiPStatus;
import hr.fer.zemris.srsv.AiPEnums.AiPType;

public class AiPMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private int entityId;
	private AiPType type;
	private AiPDirection direction;
	private AiPStatus status;
	private AiPPosition position;

	public AiPMessage(
		int entityId, AiPType type, AiPDirection direction, AiPStatus status, AiPPosition position) {
		super();
		this.entityId = entityId;
		this.type = type;
		this.direction = direction;
		this.status = status;
		this.position = position;
	}

	public int getEntityId() { return entityId; }

	public void setEntityId(int entityId) { this.entityId = entityId; }

	public AiPType getType() { return type; }

	public void setType(AiPType type) { this.type = type; }

	public AiPDirection getDirection() { return direction; }

	public void setDirection(AiPDirection direction) { this.direction = direction; }

	public AiPStatus getStatus() { return status; }

	public void setStatus(AiPStatus status) { this.status = status; }

	public AiPPosition getPosition() { return position; }

	public void setPosition(AiPPosition position) { this.position = position; }

	@Override
	public String toString() {
		return "AiPMessage [entityId="
			+ entityId + ", type=" + type + ", direction=" + direction + ", status=" + status + ", position="
			+ position + "]";
	}

}
