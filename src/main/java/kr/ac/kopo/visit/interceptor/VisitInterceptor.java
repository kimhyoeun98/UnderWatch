package kr.ac.kopo.visit.interceptor;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import kr.ac.kopo.visit.service.VisitService;

/**
 * A-04: 세션당 하루 1회만 방문수 집계(고유 방문자 근사).
 */
@Component
public class VisitInterceptor implements HandlerInterceptor {

	@Autowired
	private VisitService visitService;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		HttpSession session = request.getSession();
		String today = LocalDate.now().toString();
		Object last = session.getAttribute("visitDate");
		if (last == null || !last.equals(today)) {
			visitService.recordVisit();
			session.setAttribute("visitDate", today);
		}
		return true;
	}
}
