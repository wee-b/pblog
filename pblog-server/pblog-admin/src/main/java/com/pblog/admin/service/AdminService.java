package com.pblog.admin.service;

import com.pblog.common.domain.dto.PageQueryDTO;
import com.pblog.common.domain.dto.RegisterDTO;
import com.pblog.common.domain.dto.login.PasswordLoginDTO;
import com.pblog.common.domain.entity.User;
import com.pblog.common.domain.vo.UserAdminInfoVO;

import java.util.List;
import java.util.Map;

public interface AdminService {

    Map<String, String> passwordLogin(PasswordLoginDTO passwordLoginDTO);

    Map<String, String> register(RegisterDTO registerDTO);

    void logout();

    void edit(String username);

    List<UserAdminInfoVO> UserPageQuery(PageQueryDTO pageQueryDTO);

    User getUserAllInfo(String username);
}
