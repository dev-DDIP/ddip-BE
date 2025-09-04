package com.knu.ddip.ddipevent.domain;

public enum ActionType {
    // REQUESTER actions
    CREATE,
    SELECT_RESPONDER,
    APPROVE,
    REQUEST_REVISION,
    CANCEL_BY_REQUESTER,

    // RESPONDER actions
    APPLY,
    SUBMIT_PHOTO,
    REPORT_SITUATION,
    GIVE_UP_BY_RESPONDER,

    // SYSTEM actions
    EXPIRE,
    REWARD_PAID,
    REPORT_ISSUE;
}
