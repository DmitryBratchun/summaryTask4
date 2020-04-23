package ua.nure.bratchun.SummaryTask4.web.command.admin;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ua.nure.bratchun.SummaryTask4.Path;
import ua.nure.bratchun.SummaryTask4.exception.AppException;
import ua.nure.bratchun.SummaryTask4.web.HttpMethod;
import ua.nure.bratchun.SummaryTask4.web.command.Command;

public class CommandViewAdminPage extends Command{

	private static final long serialVersionUID = -6743607626454787498L;

	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response, HttpMethod method)
			throws IOException, ServletException, AppException {
		return Path.PAGE_ADMIN;
	}
	
}
