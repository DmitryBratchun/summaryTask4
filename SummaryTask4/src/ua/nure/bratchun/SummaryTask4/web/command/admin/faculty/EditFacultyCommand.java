package ua.nure.bratchun.SummaryTask4.web.command.admin.faculty;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import ua.nure.bratchun.SummaryTask4.Path;
import ua.nure.bratchun.SummaryTask4.db.dao.FacultyDAO;
import ua.nure.bratchun.SummaryTask4.db.dao.SubjectDAO;
import ua.nure.bratchun.SummaryTask4.db.entity.Faculty;
import ua.nure.bratchun.SummaryTask4.db.entity.Subject;
import ua.nure.bratchun.SummaryTask4.db.validation.FacultyValidation;
import ua.nure.bratchun.SummaryTask4.exception.AppException;
import ua.nure.bratchun.SummaryTask4.exception.DBException;
import ua.nure.bratchun.SummaryTask4.exception.Messages;
import ua.nure.bratchun.SummaryTask4.web.HttpMethod;
import ua.nure.bratchun.SummaryTask4.web.command.Command;

/**
 * Edit faculty command
 * 
 * @author D.Bratchun
 *
 */
public class EditFacultyCommand extends Command{
	
	private static final long serialVersionUID = 3849451275354002975L;
	private static final Logger LOG = Logger.getLogger(EditFacultyCommand.class);

	@Override
	public String execute(HttpServletRequest request, HttpServletResponse response, HttpMethod method)
			throws IOException, ServletException, AppException {
		LOG.debug("Command starts");
		String result = null;
		
		if(method == HttpMethod.POST) {
			result = doPost(request, response);
		} else {
			result = doGet(request, response);
		}
		LOG.debug("Command finished");
		return result;
	}
	
	private String doGet(HttpServletRequest request, HttpServletResponse response) throws AppException {
		int facultyId = Integer.parseInt(request.getParameter("facultyId"));
		Faculty faculty = null;
		List<Subject> preliminarySubjects = null;
		List<Subject> diplomaSubjects = null;
		
		try {	
			FacultyDAO facultyDAO = FacultyDAO.getInstance();
			faculty = facultyDAO.findById(facultyId);
			
			request.setAttribute("faculty", faculty);
			LOG.debug("request faculty " + faculty);
		} catch (DBException e) {
			LOG.error(Messages.ERR_CANNOT_GET_FACULTY, e);
			throw new AppException(Messages.ERR_CANNOT_GET_FACULTY, e);
		}
		
		try {	
			SubjectDAO subjectDAO = SubjectDAO.getInstance();
			diplomaSubjects = subjectDAO.findAll();
			preliminarySubjects = subjectDAO.getSubjectsByFacultyId(facultyId);
		} catch (DBException e) {
			LOG.error(Messages.ERR_CANNOT_GET_SUBJECTS, e);
			throw new AppException(Messages.ERR_CANNOT_GET_SUBJECTS, e);
		}
		
		String result = Path.PAGE_ERROR;
		
		List<Subject> noPreliminarySubjects = diplomaSubjects;
		Iterator<Subject> iterator = null;
		for(Subject preliminary : preliminarySubjects) {
			iterator = noPreliminarySubjects.iterator();
				while (iterator.hasNext()) {
					if(iterator.next().getId() == preliminary.getId()) {
						iterator.remove();
					}
				}
		}
		request.setAttribute("noPreliminarySubjects", noPreliminarySubjects);
		LOG.debug("request no preliminary subjects " + noPreliminarySubjects);
		
		request.setAttribute("preliminarySubjects", preliminarySubjects);
		LOG.debug("request preliminary subjects " + preliminarySubjects);
		
		result = Path.PAGE_EDIT_FACULTY;
		return result;
	}
	
	private String doPost(HttpServletRequest request, HttpServletResponse response) throws AppException  {
		
		FacultyDAO facultyDAO = null;
		Faculty faculty = null;
		int facultyId = Integer.parseInt(request.getParameter("facultyId"));
		try {
			facultyDAO = FacultyDAO.getInstance();
			if(request.getParameter("delete")!= null) {
				facultyDAO.deleteByID(facultyId);
				return Path.COMMAND_LIST_FACULTY;
			}
			faculty = facultyDAO.findById(facultyId);
		} catch (DBException e) {
			LOG.error(Messages.ERR_CANNOT_GET_FACULTY, e);
			throw new AppException(Messages.ERR_CANNOT_GET_FACULTY, e);
		}
		try {
			if(request.getParameter("delete")!= null) {
				facultyDAO.deleteByID(facultyId);
				return Path.COMMAND_LIST_FACULTY;
			}
		} catch (DBException e) {
			LOG.error(Messages.ERR_CANNOT_DELETE_FACULTY, e);
			throw new AppException(Messages.ERR_CANNOT_DELETE_FACULTY, e);
		}
		
		String newNameRu = request.getParameter("newNameRu");
		String newNameEn = request.getParameter("newNameEn");
		LOG.debug("Ru " + newNameRu + " En " + newNameEn);
		
		
		int newTotalPlaces = faculty.getTotalPlaces();
		int newBudgetPlaces= faculty.getBudgetPlaces();
		
		if(FacultyValidation.hasName(newNameEn) || FacultyValidation.hasName(newNameRu)) {
			request.getSession().setAttribute("editFacultyErrorMessage", "admin.faculties.edit_faculty_jsp.error.no_unique_name");
			return Path.COMMAND_VIEW_FACULTY +"&facultyId=" + facultyId;
		}
		
		if(newNameEn!= null && !newNameEn.isEmpty() && FacultyValidation.validationNameEn(newNameEn)) {
			faculty.setNameEn(newNameEn);
		}
		if(newNameRu!= null && !newNameRu.isEmpty() && FacultyValidation.validationNameRu(newNameRu)) {
			faculty.setNameRu(newNameRu);
		}
		if(request.getParameter("newTotalPlaces")!= null && !request.getParameter("newTotalPlaces").isEmpty()) {
			newTotalPlaces = Integer.parseInt(request.getParameter("newTotalPlaces"));
		}
		if(request.getParameter("newBudgetPlaces")!= null &&!request.getParameter("newBudgetPlaces").isEmpty()) {
			newBudgetPlaces = Integer.parseInt(request.getParameter("newBudgetPlaces"));
		}
		if(!FacultyValidation.validationPlaces(newTotalPlaces, newBudgetPlaces)) {
			request.getSession().setAttribute("editFacultyErrorMessage", "admin.faculties.edit_faculty_jsp.error.incorrect_places");
			return Path.COMMAND_VIEW_FACULTY +"&facultyId=" + facultyId;
		} else {
			faculty.setBudgetPlaces(newBudgetPlaces);
			faculty.setTotalPlaces(newTotalPlaces);
		}
			facultyDAO.update(faculty);
			facultyDAO.deleteAllPriliminaryByFacultyId(facultyId);
			facultyDAO.addAllPriliminaryByFacultyId(facultyId, getNewPreliminarySubjectsId(request, "preliminary"));
		
		
		return Path.COMMAND_LIST_FACULTY;
	}
	
	
	private List<Integer> getNewPreliminarySubjectsId(HttpServletRequest request, String parameter) {
		List<Integer> preliminarySubjectsId = new ArrayList<>();
		Enumeration<String> enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String parameterName = (String) enumeration.nextElement();
            if(parameterName.matches(parameter + "\\d+")) {
            	parameterName = parameterName.replaceAll("\\D", "");
            	preliminarySubjectsId.add(Integer.parseInt(parameterName));
            }
        }
        return preliminarySubjectsId;
	}
}
