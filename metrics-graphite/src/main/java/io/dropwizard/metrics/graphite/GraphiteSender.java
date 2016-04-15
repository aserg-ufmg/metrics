package io.dropwizard.metrics.graphite;

import java.io.Closeable;
import java.io.IOException;

public interface GraphiteSender extends Closeable{

	/**
	 * Connects to the server.
	 *
	 * @throws IllegalStateException if the client is already connected
	 * @throws IOException if there is an error connecting
	 */
	public void connect() throws IllegalStateException, IOException;

	/**
	 * Sends the given measurement to the server.
	 *
	 * @param name         the name of the metric
	 * @param value        the value of the metric
	 * @param timestamp    the timestamp of the metric
	 * @throws IOException if there was an error sending the metric
	 */
	public void sendData(String name, String value, long timestamp)
			throws IOException;

	/**
	 * Flushes buffer, if applicable
	 *
	 * @throws IOException
	 */
	void flushData() throws IOException;

	/**
	 * Returns true if ready to send data
	 */
	boolean isConnected();

	/**
	 * Returns the number of failed writes to the server.
	 *
	 * @return the number of failed writes to the server
	 */
	public int getFailures();

}