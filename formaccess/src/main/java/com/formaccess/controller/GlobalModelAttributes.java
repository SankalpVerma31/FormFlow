package com.formaccess.controller;

import com.formaccess.entity.enums.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/** Values every page may need (dropdown lists, current URL for the sidebar highlight). */
@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) { return request.getRequestURI(); }

    @ModelAttribute("questionTypes")
    public QuestionType[] questionTypes() { return QuestionType.values(); }

    @ModelAttribute("ruleAttributes")
    public RuleAttribute[] ruleAttributes() { return RuleAttribute.values(); }

    @ModelAttribute("ruleOperators")
    public RuleOperator[] ruleOperators() { return RuleOperator.values(); }

    @ModelAttribute("roles")
    public Role[] roles() { return Role.values(); }

    @ModelAttribute("accountStatuses")
    public AccountStatus[] accountStatuses() { return AccountStatus.values(); }

    @ModelAttribute("deadlineModes")
    public DeadlineMode[] deadlineModes() { return DeadlineMode.values(); }

    @ModelAttribute("visibilities")
    public Visibility[] visibilities() { return Visibility.values(); }
}
