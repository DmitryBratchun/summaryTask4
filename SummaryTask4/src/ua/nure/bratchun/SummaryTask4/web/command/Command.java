package ua.nure.bratchun.SummaryTask4.web.command;

import java.io.IOException;
import java.io.Serializable;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ua.nure.bratchun.SummaryTask4.exception.AppException;
import ua.nure.bratchun.SummaryTask4.web.HttpMethod;


/**
 * Main interface for the Command pattern implementation.
 * 
 * @author D.Bratchun
 *
 */

public abstract class Command implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * Execution method for command.
	 * 
	 * @return Address to go once the command is executed.
	 */
	public abstract String execute(HttpServletRequest request,
			HttpServletResponse response, HttpMethod method) throws IOException, ServletException,
			AppException;
	@Override
	public final String toString() {
		return getClass().getSimpleName();
	}
}
