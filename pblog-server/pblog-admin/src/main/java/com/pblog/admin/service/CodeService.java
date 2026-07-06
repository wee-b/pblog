package com.pblog.admin.service;

import com.pblog.common.domain.vo.CaptchaVO;

public interface CodeService {

    CaptchaVO generateCaptcha();

    boolean verifyCaptcha(String captchaUuid, String userInputCode);
}
