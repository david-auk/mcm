package com.mcm.backend.app.database.models.server.utils.rcon;

import java.net.Socket;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * Simple stub RCON client with placeholder authentication values.
 * TODO: Replace stub logic with a real RCON protocol implementation.
 */
public class RconClient {
    private static final String HOST = "127.0.0.1" ;
    private final int port;
    private final String password;
    // Tracks the request IDs for RCON packets
    private int requestId = 0;

    /**
     * @param port     the RCON port (e.g. 25575)
     * @param password the RCON authentication password
     */
    public RconClient(int port, String password) {
        this.port = port;
        this.password = password;
    }

    /**
     * Sends an RCON authentication followed by the given command.
     */
    public String sendCommand(String command) {
        try (Socket socket = new Socket(HOST, port);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            // Authenticate
            sendPacket(out, 3, password.getBytes(StandardCharsets.UTF_8));
            Response authResponse = readResponse(in);
            if (authResponse.id == -1) {
                throw new RuntimeException("RCON authentication failed");
            }
            // Execute command
            sendPacket(out, 2, command.getBytes(StandardCharsets.UTF_8));
            Response cmdResponse = readResponse(in);
            return cmdResponse.body;
        } catch (IOException e) {
            throw new RuntimeException("RCON communication failed", e);
        }
    }

    /**
     * Builds and sends a single RCON packet of the given type and payload.
     */
    private void sendPacket(DataOutputStream out, int type, byte[] payload) throws IOException {
        int id = ++requestId;
        // packet length = id(4) + type(4) + payload + 2 null bytes
        int length = 4 + 4 + payload.length + 2;
        ByteBuffer buf = ByteBuffer.allocate(length + 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(length);
        buf.putInt(id);
        buf.putInt(type);
        buf.put(payload);
        buf.put((byte) 0);
        buf.put((byte) 0);
        out.write(buf.array());
        out.flush();
    }

    /**
     * Reads a single RCON response packet from the input stream.
     */
    private Response readResponse(DataInputStream in) throws IOException {
        int length = Integer.reverseBytes(in.readInt());
        int id = Integer.reverseBytes(in.readInt());
        int type = Integer.reverseBytes(in.readInt());
        byte[] bodyBytes = new byte[length - 8];
        in.readFully(bodyBytes);
        // strip trailing nulls
        String body = new String(bodyBytes, StandardCharsets.UTF_8).replaceAll("\u0000+$", "");
        return new Response(id, type, body);
    }

    /**
         * Simple holder for parsed RCON response data.
         */
        private record Response(int id, int type, String body) {
    }
}