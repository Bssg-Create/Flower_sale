package com.flower.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flower.dto.LoginDto;
import com.flower.dto.UserDto;
import com.flower.entity.User;
import com.flower.exception.BaseException;
import com.flower.mapper.UserMapper;
import com.flower.service.UserService;
import com.flower.utils.JwtUtil;
import com.flower.vo.LoginVo;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserMapper userMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginVo login(LoginDto loginDto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDto.getUsername());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new BaseException(401, "用户名或密码错误");
        }
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new BaseException(401, "用户名或密码错误");
        }
        if (loginDto.getUserType() != null && !loginDto.getUserType().isEmpty()
                && !loginDto.getUserType().equals(user.getUserType())) {
            throw new BaseException(401, "用户类型不匹配，请选择正确的登录方式");
        }
        if ("0".equals(user.getStatus())) {
            throw new BaseException(403, "账号已被禁用");
        }
        LoginVo loginVo = new LoginVo();
        BeanUtils.copyProperties(user, loginVo);
        loginVo.setToken(JwtUtil.createToken(user.getId(), user.getUserType()));
        return loginVo;
    }

    @Override
    public User register(UserDto userDto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, userDto.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new BaseException("用户名已存在");
        }
        User user = new User();
        BeanUtils.copyProperties(userDto, user);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setStatus("1");
        user.setUserType(userDto.getUserType() != null ? userDto.getUserType() : "user");
        userMapper.insert(user);
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public List<User> listAll() {
        return userMapper.selectList(null);
    }

    @Override
    public boolean update(User user) {
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean delete(Long id) {
        return userMapper.deleteById(id) > 0;
    }
}