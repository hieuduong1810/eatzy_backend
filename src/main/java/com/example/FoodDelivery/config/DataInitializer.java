package com.example.FoodDelivery.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.FoodDelivery.domain.*;
import com.example.FoodDelivery.repository.*;
import com.example.FoodDelivery.service.WalletService;
import com.example.FoodDelivery.util.constant.GenderEnum;
import com.example.FoodDelivery.util.constant.StatusEnum;

import lombok.extern.slf4j.Slf4j;

@Component
@Order(2) // Run after PermissionInitializer (Order 1)
@Slf4j
public class DataInitializer implements CommandLineRunner {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final PermissionRepository permissionRepository;
        private final PasswordEncoder passwordEncoder;
        private final WalletService walletService;
        private final WalletRepository walletRepository;
        private final DriverProfileRepository driverProfileRepository;
        private final RestaurantTypeRepository restaurantTypeRepository;
        private final RestaurantRepository restaurantRepository;
        private final DishCategoryRepository dishCategoryRepository;
        private final DishRepository dishRepository;
        private final MenuOptionGroupRepository menuOptionGroupRepository;
        private final MenuOptionRepository menuOptionRepository;
        private final SystemConfigurationRepository systemConfigurationRepository;
        private final VoucherRepository voucherRepository;

        public DataInitializer(
                        UserRepository userRepository,
                        RoleRepository roleRepository,
                        PermissionRepository permissionRepository,
                        PasswordEncoder passwordEncoder,
                        WalletService walletService,
                        WalletRepository walletRepository,
                        DriverProfileRepository driverProfileRepository,
                        RestaurantTypeRepository restaurantTypeRepository,
                        RestaurantRepository restaurantRepository,
                        DishCategoryRepository dishCategoryRepository,
                        DishRepository dishRepository,
                        MenuOptionGroupRepository menuOptionGroupRepository,
                        MenuOptionRepository menuOptionRepository,
                        SystemConfigurationRepository systemConfigurationRepository,
                        VoucherRepository voucherRepository) {
                this.userRepository = userRepository;
                this.roleRepository = roleRepository;
                this.permissionRepository = permissionRepository;
                this.passwordEncoder = passwordEncoder;
                this.walletService = walletService;
                this.walletRepository = walletRepository;
                this.driverProfileRepository = driverProfileRepository;
                this.restaurantTypeRepository = restaurantTypeRepository;
                this.restaurantRepository = restaurantRepository;
                this.dishCategoryRepository = dishCategoryRepository;
                this.dishRepository = dishRepository;
                this.menuOptionGroupRepository = menuOptionGroupRepository;
                this.menuOptionRepository = menuOptionRepository;
                this.systemConfigurationRepository = systemConfigurationRepository;
                this.voucherRepository = voucherRepository;
        }

        @Override
        public void run(String... args) throws Exception {
                log.info("========== Starting Initial Data Setup ==========");

                // Check if admin user already exists
                User existingAdmin = userRepository.findByEmail("admin@gmail.com");
                if (existingAdmin != null) {
                        log.info("Initial data already exists. Skipping initialization.");
                        log.info("=======================================================");
                        return;
                }

                // 1. Create or get ADMIN role
                Role adminRole = roleRepository.findByName("ADMIN");
                if (adminRole == null) {
                        log.info("Creating ADMIN role...");
                        adminRole = new Role();
                        adminRole.setName("ADMIN");
                        adminRole.setDescription("Super admin with full access");
                        adminRole.setActive(true);

                        // Get all permissions from database
                        List<Permission> allPermissions = permissionRepository.findAll();
                        adminRole.setPermissions(allPermissions);

                        adminRole = roleRepository.save(adminRole);
                        log.info("✅ ADMIN role created with {} permissions", allPermissions.size());
                } else {
                        // Update existing ADMIN role with all permissions
                        List<Permission> allPermissions = permissionRepository.findAll();
                        adminRole.setPermissions(allPermissions);
                        adminRole = roleRepository.save(adminRole);
                        log.info("✅ ADMIN role updated with {} permissions", allPermissions.size());
                }

                // 2. Create users
                log.info("Creating users...");

                // Admin user
                User adminUser = new User();
                adminUser.setId(1L);
                adminUser.setName("admin");
                adminUser.setEmail("admin@gmail.com");
                adminUser.setPassword(passwordEncoder.encode("123456"));
                adminUser.setGender(GenderEnum.MALE);
                adminUser.setAddress("tp hcm");
                adminUser.setAge(20);
                adminUser.setIsActive(true);
                adminUser.setRole(adminRole);
                adminUser = userRepository.save(adminUser);
                log.info("✅ Admin user created");

                // Restaurant user
                User restaurantUser = new User();
                restaurantUser.setId(2L);
                restaurantUser.setName("restaurant");
                restaurantUser.setEmail("restaurant@gmail.com");
                restaurantUser.setPassword(passwordEncoder.encode("123456"));
                restaurantUser.setGender(GenderEnum.FEMALE);
                restaurantUser.setAddress("tp hcm");
                restaurantUser.setAge(25);
                restaurantUser.setIsActive(true);
                restaurantUser.setRole(adminRole);
                restaurantUser = userRepository.save(restaurantUser);
                log.info("✅ Restaurant user created");

                // Driver user
                User driverUser = new User();
                driverUser.setId(3L);
                driverUser.setName("driver");
                driverUser.setEmail("driver@gmail.com");
                driverUser.setPassword(passwordEncoder.encode("123456"));
                driverUser.setGender(GenderEnum.MALE);
                driverUser.setAddress("tp hcm");
                driverUser.setAge(30);
                driverUser.setIsActive(true);
                driverUser.setRole(adminRole);
                driverUser = userRepository.save(driverUser);
                log.info("✅ Driver user created");

                // Customer user
                User customerUser = new User();
                customerUser.setId(4L);
                customerUser.setName("customer");
                customerUser.setEmail("customer@gmail.com");
                customerUser.setPassword(passwordEncoder.encode("123456"));
                customerUser.setGender(GenderEnum.FEMALE);
                customerUser.setAddress("tp hcm");
                customerUser.setAge(22);
                customerUser.setIsActive(true);
                customerUser.setRole(adminRole);
                customerUser = userRepository.save(customerUser);
                log.info("✅ Customer user created");

                // 3. Create wallets for all users with initial balance
                log.info("Creating wallets...");
                createWalletWithBalance(adminUser, new BigDecimal("10000000")); // 10 million VND
                createWalletWithBalance(restaurantUser, new BigDecimal("5000000")); // 5 million VND
                createWalletWithBalance(driverUser, new BigDecimal("3000000")); // 3 million VND
                createWalletWithBalance(customerUser, new BigDecimal("2000000")); // 2 million VND
                log.info("✅ All wallets created with initial balance");

                // 4. Create driver profile
                log.info("Creating driver profile...");
                DriverProfile driverProfile = DriverProfile.builder()
                                .user(driverUser)
                                .vehicleDetails("Yamaha Sirius 110cc")
                                .status("AVAILABLE")
                                .currentLatitude(new BigDecimal("10.762622"))
                                .currentLongitude(new BigDecimal("106.660172"))
                                .averageRating(new BigDecimal("4.85"))
                                .codLimit(new BigDecimal("2000000"))
                                .completedTrips(150)
                                .nationalIdFront("front.jpg")
                                .nationalIdBack("back.jpg")
                                .nationalIdNumber("079203004455")
                                .nationalIdStatus(StatusEnum.APPROVED)
                                .profilePhoto("profile.jpg")
                                .profilePhotoStatus(StatusEnum.APPROVED)
                                .driverLicenseImage("license.jpg")
                                .driverLicenseNumber("B123456789")
                                .driverLicenseClass("A1")
                                .driverLicenseExpiry(LocalDate.of(2027, 12, 31))
                                .driverLicenseStatus(StatusEnum.APPROVED)
                                .bankName("Vietcombank")
                                .bankBranch("Tan Binh Branch")
                                .bankAccountHolder("Nguyen Van A")
                                .bankAccountNumber("0123456789")
                                .taxCode("1234567890")
                                .bankAccountImage("bank.jpg")
                                .bankAccountStatus(StatusEnum.APPROVED)
                                .vehicleType("Motorbike")
                                .vehicleBrand("Yamaha")
                                .vehicleModel("Sirius")
                                .vehicleLicensePlate("59X3-123.45")
                                .vehicleYear(2020)
                                .vehicleRegistrationImage("registration.jpg")
                                .vehicleRegistrationStatus(StatusEnum.APPROVED)
                                .vehicleInsuranceImage("insurance.jpg")
                                .vehicleInsuranceExpiry(LocalDate.of(2026, 5, 20))
                                .vehicleInsuranceStatus(StatusEnum.APPROVED)
                                .vehiclePhoto("vehicle_photo.jpg")
                                .vehiclePhotoStatus(StatusEnum.APPROVED)
                                .criminalRecordImage("criminal.jpg")
                                .criminalRecordNumber("CR123456")
                                .criminalRecordIssueDate(LocalDate.of(2024, 1, 10))
                                .criminalRecordStatus(StatusEnum.APPROVED)
                                .build();
                driverProfileRepository.save(driverProfile);
                log.info("✅ Driver profile created");

                // 5. Create restaurant type
                log.info("Creating restaurant types...");
                RestaurantType bunBoType = RestaurantType.builder()
                                .name("Bún bò")
                                .displayOrder(1)
                                .build();
                bunBoType = restaurantTypeRepository.save(bunBoType);
                log.info("✅ Restaurant type created");

                // 6. Create restaurant
                log.info("Creating restaurant...");
                Restaurant restaurant = Restaurant.builder()
                                .owner(restaurantUser)
                                .name("Bún bò mỡ nổi cô Như")
                                .address("232 Lý Thường Kiệt Phường Diên Hồng")
                                .contactPhone("0963298168")
                                .status("OPEN")
                                .schedule("09:00-21:00")
                                .latitude(new BigDecimal("10.77275706"))
                                .longitude(new BigDecimal("106.6855084092"))
                                .description(
                                                "Quán bún bò mỡ nổi nằm sâu trong một con hẻm ở quận 3 (TP.HCM). Một hương vị suốt 30 năm, từ đời mẹ sang đời con nay thì vô cùng đông khách khi được Michelin lựa chọn gọi tên.")
                                .restaurantTypes(List.of(bunBoType))
                                .slug("bun-bo-mo-noi-co-nhu")
                                .build();
                restaurant = restaurantRepository.save(restaurant);
                log.info("✅ Restaurant created");

                // 7. Create dish categories
                log.info("Creating dish categories...");
                DishCategory signatureBowls = DishCategory.builder()
                                .restaurant(restaurant)
                                .name("Signature Bowls")
                                .displayOrder(1)
                                .build();
                signatureBowls = dishCategoryRepository.save(signatureBowls);

                DishCategory sideDishes = DishCategory.builder()
                                .restaurant(restaurant)
                                .name("Side Dishes")
                                .displayOrder(2)
                                .build();
                sideDishes = dishCategoryRepository.save(sideDishes);

                DishCategory drinks = DishCategory.builder()
                                .restaurant(restaurant)
                                .name("Drinks")
                                .displayOrder(3)
                                .build();
                drinks = dishCategoryRepository.save(drinks);
                log.info("✅ Dish categories created");

                // 8. Create dishes
                log.info("Creating dishes...");
                Dish bunBoDacBiet = Dish.builder()
                                .restaurant(restaurant)
                                .category(signatureBowls)
                                .name("Bún bò mỡ nổi đặc biệt")
                                .description("Tô đặc biệt nhiều thịt, chả, giò và mỡ nổi đúng chuẩn")
                                .price(new BigDecimal("65000"))
                                .imageUrl("https://example.com/bun-bo-mo-noi-dac-biet.jpg")
                                .availabilityQuantity(50)
                                .build();
                bunBoDacBiet = dishRepository.save(bunBoDacBiet);

                Dish bunBoThuong = Dish.builder()
                                .restaurant(restaurant)
                                .category(signatureBowls)
                                .name("Bún bò mỡ nổi tô thường")
                                .description("Tô thường với bò và nước dùng mỡ nổi béo thơm")
                                .price(new BigDecimal("50000"))
                                .imageUrl("https://example.com/bun-bo-mo-noi-thuong.jpg")
                                .availabilityQuantity(50)
                                .build();
                bunBoThuong = dishRepository.save(bunBoThuong);

                Dish chaCua = Dish.builder()
                                .restaurant(restaurant)
                                .category(sideDishes)
                                .name("Chả cua handmade")
                                .description("Chả cua dai thơm, ăn kèm bún bò")
                                .price(new BigDecimal("15000"))
                                .imageUrl("https://example.com/cha-cua.jpg")
                                .availabilityQuantity(30)
                                .build();
                chaCua = dishRepository.save(chaCua);

                Dish gioBo = Dish.builder()
                                .restaurant(restaurant)
                                .category(sideDishes)
                                .name("Giò bò")
                                .description("Khoanh giò bò chất lượng cao")
                                .price(new BigDecimal("15000"))
                                .imageUrl("https://example.com/gio-bo.jpg")
                                .availabilityQuantity(25)
                                .build();
                gioBo = dishRepository.save(gioBo);

                Dish traDa = Dish.builder()
                                .restaurant(restaurant)
                                .category(drinks)
                                .name("Trà đá")
                                .description("Ly trà đá mát lạnh")
                                .price(new BigDecimal("5000"))
                                .imageUrl("https://example.com/tra-da.jpg")
                                .availabilityQuantity(200)
                                .build();
                traDa = dishRepository.save(traDa);

                Dish samBiDao = Dish.builder()
                                .restaurant(restaurant)
                                .category(drinks)
                                .name("Sâm bí đao")
                                .description("Nước sâm bí đao nấu tại quán")
                                .price(new BigDecimal("12000"))
                                .imageUrl("https://example.com/sam-bi-dao.jpg")
                                .availabilityQuantity(100)
                                .build();
                samBiDao = dishRepository.save(samBiDao);
                log.info("✅ Dishes created");

                // 9. Create menu option groups and options
                log.info("Creating menu option groups and options...");

                // For Bún bò đặc biệt
                MenuOptionGroup mucMoGroup1 = MenuOptionGroup.builder()
                                .dish(bunBoDacBiet)
                                .groupName("Mức mỡ")
                                .minChoices(1)
                                .maxChoices(1)
                                .build();
                mucMoGroup1 = menuOptionGroupRepository.save(mucMoGroup1);

                MenuOptionGroup toppingGroup1 = MenuOptionGroup.builder()
                                .dish(bunBoDacBiet)
                                .groupName("Topping thêm")
                                .minChoices(0)
                                .maxChoices(5)
                                .build();
                toppingGroup1 = menuOptionGroupRepository.save(toppingGroup1);

                // For Bún bò thường
                MenuOptionGroup mucMoGroup2 = MenuOptionGroup.builder()
                                .dish(bunBoThuong)
                                .groupName("Mức mỡ")
                                .minChoices(1)
                                .maxChoices(1)
                                .build();
                mucMoGroup2 = menuOptionGroupRepository.save(mucMoGroup2);

                MenuOptionGroup toppingGroup2 = MenuOptionGroup.builder()
                                .dish(bunBoThuong)
                                .groupName("Topping thêm")
                                .minChoices(0)
                                .maxChoices(5)
                                .build();
                toppingGroup2 = menuOptionGroupRepository.save(toppingGroup2);

                // Create menu options for group 1 (Mức mỡ - Bún bò đặc biệt)
                List<MenuOption> menuOptions1 = List.of(
                                MenuOption.builder().menuOptionGroup(mucMoGroup1).name("Ít mỡ")
                                                .priceAdjustment(BigDecimal.ZERO)
                                                .isAvailable(true).build(),
                                MenuOption.builder().menuOptionGroup(mucMoGroup1).name("Vừa")
                                                .priceAdjustment(BigDecimal.ZERO)
                                                .isAvailable(true).build(),
                                MenuOption.builder().menuOptionGroup(mucMoGroup1).name("Nhiều mỡ")
                                                .priceAdjustment(BigDecimal.ZERO)
                                                .isAvailable(true).build());
                menuOptionRepository.saveAll(menuOptions1);

                // Create menu options for group 2 (Topping thêm - Bún bò đặc biệt)
                List<MenuOption> menuOptions2 = List.of(
                                MenuOption.builder().menuOptionGroup(toppingGroup1).name("Chả cua thêm")
                                                .priceAdjustment(new BigDecimal("15000")).isAvailable(true).build(),
                                MenuOption.builder().menuOptionGroup(toppingGroup1).name("Giò bò thêm")
                                                .priceAdjustment(new BigDecimal("15000")).isAvailable(true).build(),
                                MenuOption.builder().menuOptionGroup(toppingGroup1).name("Thịt bò thêm")
                                                .priceAdjustment(new BigDecimal("20000")).isAvailable(true).build(),
                                MenuOption.builder().menuOptionGroup(toppingGroup1).name("Huyết thêm")
                                                .priceAdjustment(new BigDecimal("5000")).isAvailable(true).build());
                menuOptionRepository.saveAll(menuOptions2);

                // Create menu options for group 3 (Mức mỡ - Bún bò thường)
                List<MenuOption> menuOptions3 = List.of(
                                MenuOption.builder().menuOptionGroup(mucMoGroup2).name("Ít mỡ")
                                                .priceAdjustment(BigDecimal.ZERO)
                                                .isAvailable(true).build(),
                                MenuOption.builder().menuOptionGroup(mucMoGroup2).name("Vừa")
                                                .priceAdjustment(BigDecimal.ZERO)
                                                .isAvailable(true).build(),
                                MenuOption.builder().menuOptionGroup(mucMoGroup2).name("Nhiều mỡ")
                                                .priceAdjustment(BigDecimal.ZERO)
                                                .isAvailable(true).build());
                menuOptionRepository.saveAll(menuOptions3);

                // Create menu options for group 4 (Topping thêm - Bún bò thường)
                List<MenuOption> menuOptions4 = List.of(
                                MenuOption.builder().menuOptionGroup(toppingGroup2).name("Chả cua thêm")
                                                .priceAdjustment(new BigDecimal("15000")).isAvailable(true).build(),
                                MenuOption.builder().menuOptionGroup(toppingGroup2).name("Giò bò thêm")
                                                .priceAdjustment(new BigDecimal("15000")).isAvailable(true).build(),
                                MenuOption.builder().menuOptionGroup(toppingGroup2).name("Thịt bò thêm")
                                                .priceAdjustment(new BigDecimal("20000")).isAvailable(true).build(),
                                MenuOption.builder().menuOptionGroup(toppingGroup2).name("Huyết thêm")
                                                .priceAdjustment(new BigDecimal("5000")).isAvailable(true).build());
                menuOptionRepository.saveAll(menuOptions4);
                log.info("✅ Menu option groups and options created");

                // 10. Create system configuration
                log.info("Creating system configuration...");
                List<SystemConfiguration> configs = new ArrayList<>();

                configs.add(createConfig("RESTAURANT_COMMISSION_RATE", "15",
                                "Tỉ lệ hoa hồng sàn thu của Quán (0.15 = 15%)",
                                adminUser));
                configs.add(createConfig("DRIVER_COMMISSION_RATE", "20",
                                "Tỉ lệ hoa hồng sàn thu từ phí ship của Tài xế (0.20 = 20%)", adminUser));
                configs.add(createConfig("DELIVERY_BASE_FEE", "15000", "Phí ship cơ bản (VND)", adminUser));
                configs.add(createConfig("DELIVERY_BASE_DISTANCE", "3", "Khoảng cách áp dụng phí cơ bản (km)",
                                adminUser));
                configs.add(createConfig("DELIVERY_PER_KM_FEE", "5000", "Phí ship cộng thêm cho mỗi km tiếp theo (VND)",
                                adminUser));
                configs.add(createConfig("DRIVER_SEARCH_RADIUS_KM", "5",
                                "Bán kính tìm kiếm tài xế xung quanh quán (km)",
                                adminUser));
                configs.add(createConfig("DRIVER_ACCEPT_TIMEOUT_SEC", "30",
                                "Thời gian tối đa để tài xế quyết định nhận đơn (giây)", adminUser));
                configs.add(createConfig("DRIVER_MIN_WALLET_BALANCE", "50000",
                                "Số dư ví tối thiểu để tài xế được nhận đơn (VND)", adminUser));
                configs.add(createConfig("DRIVER_DEFAULT_COD_LIMIT", "2000000",
                                "Giới hạn tiền thu hộ mặc định cho tài xế mới (VND)", adminUser));
                configs.add(createConfig("MAX_RESTAURANT_DISTANCE_KM", "20",
                                "Khoảng cách tối đa khách được phép đặt hàng (km)",
                                adminUser));
                configs.add(createConfig("RESTAURANT_RESPONSE_TIMEOUT_MINUTES", "15",
                                "Tự động hủy đơn nếu quán không xác nhận sau X phút", adminUser));
                configs.add(createConfig("DRIVER_ASSIGNMENT_TIMEOUT_MINUTES", "30",
                                "Tự động hủy đơn nếu không tìm thấy tài xế sau X phút", adminUser));
                configs.add(createConfig("SUPPORT_HOTLINE", "19001234", "Số điện thoại tổng đài hỗ trợ", adminUser));
                configs.add(createConfig("MAINTENANCE_MODE", "false", "Trạng thái bảo trì hệ thống (true/false)",
                                adminUser));

                systemConfigurationRepository.saveAll(configs);
                log.info("✅ System configuration created");

                // 11. Create vouchers
                log.info("Creating vouchers...");
                List<Voucher> vouchers = new ArrayList<>();

                vouchers.add(Voucher.builder()
                                .code("CHAO2025")
                                .description("Giảm 30k cho đơn từ 100k cho khách hàng mới")
                                .discountType("FIXED")
                                .discountValue(new BigDecimal("30000.00"))
                                .endDate(Instant.parse("2026-01-31T16:59:59.000Z"))
                                .minOrderValue(new BigDecimal("100000.00"))
                                .startDate(Instant.parse("2024-12-31T17:00:00.000Z"))
                                .totalQuantity(1000)
                                .restaurant(restaurant)
                                .build());

                vouchers.add(Voucher.builder()
                                .code("SIEUDEAL50")
                                .description("Giảm 50% tối đa 50k cho mọi đơn hàng")
                                .discountType("PERCENTAGE")
                                .discountValue(new BigDecimal("50.00"))
                                .endDate(Instant.parse("2026-01-31T16:59:59.000Z"))
                                .minOrderValue(new BigDecimal("0.00"))
                                .startDate(Instant.parse("2024-12-31T17:00:00.000Z"))
                                .totalQuantity(500)
                                .restaurant(restaurant)
                                .maxDiscountAmount(new BigDecimal("50000.00"))
                                .build());

                vouchers.add(Voucher.builder()
                                .code("FREESHIP")
                                .description("miễn phí giao hàng")
                                .discountType("FREESHIP")
                                .endDate(Instant.parse("2026-01-31T16:59:59.000Z"))
                                .minOrderValue(new BigDecimal("0.00"))
                                .startDate(Instant.parse("2024-12-31T17:00:00.000Z"))
                                .totalQuantity(500)
                                .restaurant(restaurant)
                                .build());

                voucherRepository.saveAll(vouchers);
                log.info("✅ Vouchers created");

                log.info("========== Initial Data Setup Complete ==========");
                log.info("Users created:");
                log.info("   1. admin@gmail.com (password: 123456) - Wallet: 10,000,000 VND");
                log.info("   2. restaurant@gmail.com (password: 123456) - Wallet: 5,000,000 VND");
                log.info("   3. driver@gmail.com (password: 123456) - Wallet: 3,000,000 VND");
                log.info("   4. customer@gmail.com (password: 123456) - Wallet: 2,000,000 VND");
                log.info("Restaurant: Bún bò mỡ nổi cô Như (6 dishes, 14 menu options, 3 vouchers)");
                log.info("System configuration: 14 settings");
                log.info("=======================================================");
        }

        private void createWalletWithBalance(User user, BigDecimal balance) {
                walletService.createWalletForUser(user);
                Wallet wallet = walletRepository.findByUserId(user.getId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Wallet not found for user: " + user.getEmail()));
                wallet.setBalance(balance);
                walletRepository.save(wallet);
        }

        private SystemConfiguration createConfig(String key, String value, String description, User updatedBy) {
                return SystemConfiguration.builder()
                                .configKey(key)
                                .configValue(value)
                                .description(description)
                                .lastUpdatedBy(updatedBy)
                                .updatedAt(Instant.now())
                                .build();
        }
}
