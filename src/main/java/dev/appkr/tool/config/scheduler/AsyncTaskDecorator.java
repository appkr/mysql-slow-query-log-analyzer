package dev.appkr.tool.config.scheduler;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class AsyncTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    final Map<String, String> contextMap = MDC.getCopyOfContextMap();
    if (contextMap == null) {
      /**
       * WORKAROUND for "Exception in thread "exe-3" java.lang.NullPointerException
       * @see https://jira.qos.ch/browse/LOGBACK-944
       */
      return () -> runnable.run();
    } else {
      return () -> {
        MDC.setContextMap(contextMap);
        try {
          runnable.run();
        } finally {
          MDC.clear();
        }
      };
    }
  }
}
