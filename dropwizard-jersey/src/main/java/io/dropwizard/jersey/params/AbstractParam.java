package io.dropwizard.jersey.params;

import io.dropwizard.jersey.errors.ErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * An abstract base class from which to build Jersey parameter classes.
 *
 * @param <T> the type of value wrapped by the parameter
 */
public abstract class AbstractParam<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractParam.class);
    private final String parameterName;
    private final T value;

    protected AbstractParam(@Nullable String input) {
        this(input, "Parameter");
    }

    /**
     * Given an input value from a client, creates a parameter wrapping its parsed value.
     *
     * @param input an input value from a client request
     */
    protected AbstractParam(@Nullable String input, String parameterName) {
        this.parameterName = parameterName;
        try {
            this.value = parse(input);
        } catch (Exception e) {
            throw new WebApplicationException(error(input, e));
        }
    }

    /**
     * Given a string representation which was unable to be parsed and the exception thrown, produce
     * a {@link Response} to be sent to the client.
     *
     * By default, generates a {@code 400 Bad Request} with a plain text entity generated by
     * {@link #errorMessage(Exception)}.
     *
     * @param input the raw input value
     * @param e the exception thrown while parsing {@code input}
     * @return the {@link Response} to be sent to the client
     */
    protected Response error(@Nullable String input, Exception e) {
        LOGGER.debug("Invalid input received: {}", input);
        String errorMessage = errorMessage(e);
        if (errorMessage.contains("%s")) {
            errorMessage = String.format(errorMessage, parameterName);
        }
        return Response.status(getErrorStatus())
                       .entity(new ErrorMessage(getErrorStatus().getStatusCode(),
                               errorMessage))
                       .type(mediaType())
                       .build();
    }

    /**
     * Returns the media type of the error message entity.
     *
     * @return the media type of the error message entity
     */
    protected MediaType mediaType() {
        return MediaType.APPLICATION_JSON_TYPE;
    }

    /**
     * Given a string representation which was unable to be parsed and the exception thrown, produce
     * an entity to be sent to the client.
     *
     * @param e the exception thrown while parsing {@code input}
     * @return the error message to be sent the client
     */
    protected String errorMessage(Exception e) {
        return String.format("%s is invalid: %s", parameterName, e.getMessage());
    }

    /**
     * Given a string representation which was unable to be parsed, produce a {@link Status} for the
     * {@link Response} to be sent to the client.
     *
     * @return the HTTP {@link Status} of the error message
     */
    @SuppressWarnings("MethodMayBeStatic")
    protected Status getErrorStatus() {
        return Status.BAD_REQUEST;
    }

   /**
    * Given a string representation, parse it and return an instance of the parameter type.
    *
    * @param input the raw input
    * @return {@code input}, parsed as an instance of {@code T}
    * @throws Exception if there is an error parsing the input
    */
    protected abstract T parse(@Nullable String input) throws Exception;

    /**
     * Returns the underlying value.
     *
     * @return the underlying value
     */
    public T get() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final AbstractParam<?> that = (AbstractParam<?>) obj;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toString();
    }
}