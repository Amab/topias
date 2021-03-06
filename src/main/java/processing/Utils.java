package processing;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.VcsRoot;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.impl.ProjectLevelVcsManagerImpl;
import com.intellij.psi.*;
import git4idea.GitReference;
import git4idea.repo.GitRepository;
import git4idea.repo.GitRepositoryManager;
import gr.uom.java.xmi.UMLOperation;

import java.util.*;

public final class Utils {

    public static String calculateSignature(PsiMethod method) {
        final PsiClass containingClass = method.getContainingClass();
        final String className;
        if (containingClass != null) {
            className = containingClass.getQualifiedName();
        } else {
            className = "";
        }
        final String methodName = method.getName();
        final StringBuilder out = new StringBuilder(50);
        out.append(className);
        out.append('.');
        out.append(methodName);
        out.append('(');
        final PsiParameterList parameterList = method.getParameterList();
        final PsiParameter[] parameters = parameterList.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                out.append(',');
            }
            final PsiType parameterType = parameters[i].getType();
            final String parameterTypeText = parameterType.getPresentableText();
            out.append(parameterTypeText);
        }
        out.append(')');
        return out.toString();
    }

    public static String calculateSignatureForEcl(UMLOperation operation) {
        StringBuilder builder = new StringBuilder();

        builder.append(operation.getClassName())
                .append(".")
                .append(operation.getName())
                .append("(");

        operation.getParameterTypeList().forEach(x -> builder.append(x).append(","));

        if (operation.getParameterTypeList().size() > 0)
            builder.deleteCharAt(builder.length() - 1);

        builder.append(")");
        return builder.toString();
    }

    public static String getCurrentBranchName(Project project) throws VcsException {
        final ProjectLevelVcsManagerImpl instance = (ProjectLevelVcsManagerImpl) ProjectLevelVcsManager.getInstance(project);
        final VcsRoot gitRootPath = Arrays.stream(instance.getAllVcsRoots()).filter(x -> x.getVcs() != null)
                .filter(x -> x.getVcs().getName().equalsIgnoreCase("git"))
                .findAny().orElse(null);

        if (gitRootPath == null)
            throw new VcsException("No git repository found");

        return GitRepositoryManager.getInstance(project).getRepositories().stream().filter(x -> x.getRoot().equals(gitRootPath.getPath()))
                .map(GitRepository::getCurrentBranch)
                .filter(Objects::nonNull)
                .map(GitReference::getName)
                .findFirst().orElse("master");
    }

    public static String getFileName(Change change) {
        return change.toString().substring(change.toString().indexOf(':') + 2);
    }

    public static String getOldFileName(Change change) {
        return change.toString().substring(change.toString().indexOf(':') + 2).split(" -> ")[0];
    }

    public static String getNewFileName(Change change) {
        return change.toString().substring(change.toString().indexOf(':') + 2).split(" -> ")[1];
    }

    public static String trimClassName(String fullMethodSignature) {
        final String className = fullMethodSignature.substring(0, fullMethodSignature.lastIndexOf('.'));
        return className;
    }

    public static String trimMethodName(String fullMethodName) {
        final String methodName = fullMethodName.substring(fullMethodName.lastIndexOf('.') + 1, fullMethodName.lastIndexOf('('));
        return methodName;
    }

    public static String buildDBUrlForSystem(Project project) {
        final StringBuilder pathBuilder = new StringBuilder().append(project.getBasePath());
        final String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
        if (os.contains("mac") || os.contains("darwin") || os.contains("nux")) {
            pathBuilder.append("/.idea/state.db");
        } else if (os.contains("win")) {
            pathBuilder.append("\\.idea\\state.db");
        }
        return pathBuilder.toString();
    }
}
