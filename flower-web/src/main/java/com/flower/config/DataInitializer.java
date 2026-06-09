package com.flower.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.flower.entity.*;
import com.flower.mapper.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final FlowerMapper flowerMapper;
    private final FlowerCategoryMapper flowerCategoryMapper;
    private final PackageTypeMapper packageTypeMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(UserMapper userMapper, RoleMapper roleMapper, UserRoleMapper userRoleMapper,
                           FlowerMapper flowerMapper, FlowerCategoryMapper flowerCategoryMapper,
                           PackageTypeMapper packageTypeMapper, BCryptPasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.flowerMapper = flowerMapper;
        this.flowerCategoryMapper = flowerCategoryMapper;
        this.packageTypeMapper = packageTypeMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        initAdminUser();
        initRoles();
        initCategories();
        initFlowers();
        initPackageTypes();
    }

    private void initRoles() {
        try {
            Long count = roleMapper.selectCount(null);
            if (count == 0) {
                Role adminRole = new Role();
                adminRole.setRoleName("管理员");
                adminRole.setRoleCode("admin");
                adminRole.setDescription("系统管理员");
                adminRole.setStatus("1");
                roleMapper.insert(adminRole);

                Role userRole = new Role();
                userRole.setRoleName("普通用户");
                userRole.setRoleCode("user");
                userRole.setDescription("普通用户");
                userRole.setStatus("1");
                roleMapper.insert(userRole);

                log.info("Roles seeded.");
            }
        } catch (Exception e) {
            log.warn("Role init skipped: {}", e.getMessage());
        }
    }

    private void initAdminUser() {
        try {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, "admin");
            User admin = userMapper.selectOne(wrapper);

            if (admin == null) {
                log.warn("Admin not found, creating admin/123456 ...");
                admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("123456"));
                admin.setPhone("13900000000");
                admin.setEmail("admin@flower.com");
                admin.setUserType("admin");
                admin.setStatus("1");
                userMapper.insert(admin);

                UserRole ur = new UserRole();
                ur.setUserId(admin.getId());
                ur.setRoleId(1L);
                userRoleMapper.insert(ur);

                log.info("Admin created (id={}).", admin.getId());
            } else {
                if (!passwordEncoder.matches("123456", admin.getPassword())) {
                    admin.setPassword(passwordEncoder.encode("123456"));
                    userMapper.updateById(admin);
                    log.info("Admin password reset.");
                } else {
                    log.info("Admin OK.");
                }
            }
        } catch (Exception e) {
            log.error("Admin init failed: {}", e.getMessage());
        }
    }

    private void initCategories() {
        try {
            Long count = flowerCategoryMapper.selectCount(null);
            if (count == 0) {
                String[][] data = {
                    {"玫瑰", "玫瑰花是爱情的象征", "🌹", "1"},
                    {"百合", "百合花代表纯洁高贵", "💮", "2"},
                    {"郁金香", "郁金香是荷兰国花，优雅高贵", "🌷", "3"},
                    {"向日葵", "向日葵象征阳光希望", "🌻", "4"},
                    {"康乃馨", "康乃馨是母爱的象征", "🌺", "5"},
                    {"配叶", "用于花束搭配的绿叶植物", "🌿", "10"},
                };
                for (String[] d : data) {
                    FlowerCategory c = new FlowerCategory();
                    c.setName(d[0]);
                    c.setDescription(d[1]);
                    c.setIcon(d[2]);
                    c.setSortOrder(Integer.parseInt(d[3]));
                    c.setStatus("1");
                    flowerCategoryMapper.insert(c);
                }
                log.info("Categories seeded.");
            }
        } catch (Exception e) {
            log.warn("Category init skipped: {}", e.getMessage());
        }
    }

    private void initFlowers() {
        try {
            Long count = flowerMapper.selectCount(null);
            if (count == 0) {
                Map<String, Long> catMap = new LinkedHashMap<>();
                List<FlowerCategory> cats = flowerCategoryMapper.selectList(null);
                for (FlowerCategory c : cats) catMap.put(c.getName(), c.getId());

                Object[][] data = {
                    {"红玫瑰", "玫瑰", 15.00, "精选优质红玫瑰，鲜艳欲滴，浪漫之选"},
                    {"白玫瑰", "玫瑰", 16.00, "纯洁优雅的白玫瑰，象征纯洁的爱情"},
                    {"粉玫瑰", "玫瑰", 15.00, "温柔甜美的粉玫瑰，适合表白"},
                    {"黄玫瑰", "玫瑰", 14.00, "明亮的黄玫瑰，代表友谊"},
                    {"白百合", "百合", 20.00, "洁白无瑕的百合花，高贵典雅"},
                    {"粉百合", "百合", 22.00, "粉嫩的百合花，浪漫温馨"},
                    {"红郁金香", "郁金香", 18.00, "热情奔放的红郁金香"},
                    {"粉郁金香", "郁金香", 18.00, "温柔甜美的粉郁金香"},
                    {"黄郁金香", "郁金香", 17.00, "明亮灿烂的黄郁金香"},
                    {"向日葵", "向日葵", 12.00, "阳光般的向日葵，充满活力"},
                    {"康乃馨", "康乃馨", 10.00, "温馨的康乃馨，送给最爱的人"},
                    {"尤加利叶", "配叶", 5.00, "优雅的尤加利叶，增加花束层次感"},
                    {"小雏菊", "配叶", 8.00, "可爱的小雏菊，清新淡雅"},
                };
                for (Object[] d : data) {
                    Flower f = new Flower();
                    f.setName((String) d[0]);
                    f.setCategoryId(catMap.get(d[1]));
                    f.setCategoryName((String) d[1]);
                    f.setPrice(BigDecimal.valueOf((Double) d[2]));
                    f.setDescription((String) d[3]);
                    f.setImageUrl("/images/" + toEnglishFileName((String) d[0]) + ".png");
                    f.setStock(100);
                    f.setStatus("1");
                    flowerMapper.insert(f);
                }
                log.info("Flowers seeded ({}).", data.length);
            }
        } catch (Exception e) {
            log.warn("Flower init skipped: {}", e.getMessage());
        }
    }

    private void initPackageTypes() {
        try {
            Long count = packageTypeMapper.selectCount(null);
            if (count == 0) {
                Object[][] data = {
                    {"圆形包装", "经典圆形花束包装，优雅大方", 10.00},
                    {"心形包装", "浪漫心形花束包装，适合表白", 15.00},
                    {"长形包装", "简约长形花束包装，时尚现代", 12.00},
                    {"礼盒包装", "精美礼盒包装，送礼首选", 20.00},
                };
                for (Object[] d : data) {
                    PackageType p = new PackageType();
                    p.setName((String) d[0]);
                    p.setDescription((String) d[1]);
                    p.setPrice(BigDecimal.valueOf((Double) d[2]));
                    p.setStatus("1");
                    packageTypeMapper.insert(p);
                }
                log.info("Package types seeded.");
            }
        } catch (Exception e) {
            log.warn("PackageType init skipped: {}", e.getMessage());
        }
    }

    private String toEnglishFileName(String name) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("红玫瑰", "red-rose");
        map.put("白玫瑰", "white-rose");
        map.put("粉玫瑰", "pink-rose");
        map.put("黄玫瑰", "yellow-rose");
        map.put("白百合", "white-lily");
        map.put("粉百合", "pink-lily");
        map.put("红郁金香", "red-tulip");
        map.put("粉郁金香", "pink-tulip");
        map.put("黄郁金香", "yellow-tulip");
        map.put("向日葵", "sunflower");
        map.put("康乃馨", "carnation");
        map.put("尤加利叶", "eucalyptus");
        map.put("小雏菊", "daisy");
        return map.getOrDefault(name, name);
    }
}