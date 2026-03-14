package com.workflow.engine.repository;

import com.workflow.engine.model.instance.TransitionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransitionLogRepository extends JpaRepository<TransitionLog, Long> {

    List<TransitionLog> findByWorkflowInstanceIdOrderByTransitionedAtAsc(Long workflowInstanceId);
}
