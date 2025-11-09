package engine.dto;

/**
 *
 * A class that contains action information sent from the outside (Python)
 * /action Map the request body (JSON) of the endpoint
 */
public class ActionPacket {
    /** 0: stop, 1: left, 2: right, 3: up, 4: down, 5: atack */
    public int action;
}