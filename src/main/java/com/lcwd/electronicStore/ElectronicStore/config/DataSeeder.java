package com.lcwd.electronicStore.ElectronicStore.config;

import com.lcwd.electronicStore.ElectronicStore.entities.Category;
import com.lcwd.electronicStore.ElectronicStore.entities.Product;
import com.lcwd.electronicStore.ElectronicStore.repositories.CategoryRepository;
import com.lcwd.electronicStore.ElectronicStore.repositories.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public DataSeeder(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<CategorySeed> categories = List.of(
                new CategorySeed("smartphones", "Smartphones", "Flagship Android and iOS phones with bright displays and fast cameras.", "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9"),
                new CategorySeed("laptops", "Laptops", "Everyday and creator laptops for study, office work, and entertainment.", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853"),
                new CategorySeed("audio", "Audio", "Headphones, speakers, and earbuds for music, calls, and travel.", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e"),
                new CategorySeed("home-appliances", "Home Appliances", "Smart appliances for modern kitchens, laundry, and home comfort.", "https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5"),
                new CategorySeed("gaming", "Gaming", "Consoles, controllers, and accessories for smooth gameplay.", "https://images.unsplash.com/photo-1606144042614-b2417e99c4e3"),
                new CategorySeed("accessories", "Accessories", "Chargers, docks, keyboards, watches, and useful electronics add-ons.", "https://images.unsplash.com/photo-1523275335684-37898b6baf30"),
                new CategorySeed("televisions", "Televisions", "Smart LED, QLED, Mini LED, and OLED televisions for movies, sports, and gaming.", "https://images.unsplash.com/photo-1593784991095-a205069470b6")
        );

        categories.forEach(this::upsertCategory);
        mergeDuplicateCategories();

        List<ProductSeed> products = List.of(
                new ProductSeed("seed-galaxy-s23", "Samsung Galaxy S23", "Flagship Android phone with a vivid AMOLED display and fast 5G performance.", 70000, 65000, 15, true, true, "smartphones", "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c"),
                new ProductSeed("seed-iphone-14", "iPhone 14", "Apple smartphone with excellent cameras, reliable performance, and long battery life.", 178000, 132000, 9, true, true, "smartphones", "https://images.unsplash.com/photo-1592750475338-74b7b21085ab"),
                new ProductSeed("seed-pixel-7", "Google Pixel 7", "Google flagship smartphone with clean Android and computational photography.", 65000, 60000, 12, true, true, "smartphones", "https://images.unsplash.com/photo-1598327105666-5b89351aff97"),
                new ProductSeed("seed-oneplus-12r", "OnePlus 12R", "Performance-focused phone with a bright display, fast charging, and smooth gaming.", 45999, 39999, 18, true, true, "smartphones", "https://images.unsplash.com/photo-1567581935884-3349723552ca"),
                new ProductSeed("seed-vivo-v30-pro", "Vivo V30 Pro", "Slim camera phone with portrait features, vibrant color, and fast everyday speed.", 42999, 38999, 11, true, true, "smartphones", "https://images.unsplash.com/photo-1605236453806-6ff36851218e"),
                new ProductSeed("seed-redmi-note-13-pro", "Redmi Note 13 Pro", "Budget-friendly 5G phone with AMOLED display and dependable battery life.", 28999, 24999, 28, true, true, "smartphones", "https://images.unsplash.com/photo-1595941069915-4ebc5197c14a"),
                new ProductSeed("seed-moto-edge-50-fusion", "Motorola Edge 50 Fusion", "Clean Android phone with a curved display, stereo audio, and fast charging.", 25999, 22999, 20, true, true, "smartphones", "https://images.unsplash.com/photo-1580910051074-3eb694886505"),
                new ProductSeed("seed-nothing-phone-2a", "Nothing Phone 2a", "Distinctive mid-range phone with a glyph interface and balanced performance.", 27999, 23999, 16, true, true, "smartphones", "https://images.unsplash.com/photo-1512054502232-10a0a035d672"),

                new ProductSeed("seed-dell-inspiron-15", "Dell Inspiron 15", "Reliable 15 inch laptop for study, business, browsing, and streaming.", 62000, 56000, 10, true, true, "laptops", "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed"),
                new ProductSeed("seed-macbook-air", "MacBook Air M2", "Thin and light Apple laptop with silent performance and all-day battery.", 114900, 104900, 6, true, true, "laptops", "https://images.unsplash.com/photo-1517336714731-489689fd1ca8"),
                new ProductSeed("seed-hp-pavilion-14", "HP Pavilion 14", "Compact work laptop with a crisp display, backlit keyboard, and fast SSD storage.", 68990, 61990, 14, true, true, "laptops", "https://images.unsplash.com/photo-1496181133206-80ce9b88a853"),
                new ProductSeed("seed-lenovo-ideapad-slim-5", "Lenovo IdeaPad Slim 5", "Portable productivity laptop with Ryzen performance and a durable metal body.", 74990, 66990, 12, true, true, "laptops", "https://images.unsplash.com/photo-1593642632823-8f785ba67e45"),
                new ProductSeed("seed-asus-vivobook-16", "Asus Vivobook 16", "Large-screen laptop for office, learning, entertainment, and light creative work.", 57990, 50990, 17, true, true, "laptops", "https://images.unsplash.com/photo-1484788984921-03950022c9ef"),
                new ProductSeed("seed-acer-swift-go-14", "Acer Swift Go 14", "Lightweight OLED laptop with quick boot, metal finish, and travel-friendly battery.", 84990, 75990, 8, true, true, "laptops", "https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2"),
                new ProductSeed("seed-msi-modern-15", "MSI Modern 15", "Business laptop with a numeric keypad, strong multitasking, and slim styling.", 52990, 46990, 13, true, true, "laptops", "https://images.unsplash.com/photo-1541807084-5c52b6b3adef"),
                new ProductSeed("seed-samsung-galaxy-book4", "Samsung Galaxy Book4", "Premium thin laptop with a sharp display, Intel performance, and easy portability.", 89990, 79990, 9, true, true, "laptops", "https://images.unsplash.com/photo-1498050108023-c5249f4df085"),

                new ProductSeed("seed-sony-headphones", "Sony WH-1000XM5", "Premium wireless noise cancelling headphones for travel and work.", 34990, 29990, 18, true, true, "audio", "https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb"),
                new ProductSeed("seed-jbl-speaker", "JBL Flip Speaker", "Portable Bluetooth speaker with punchy sound and splash-resistant design.", 9999, 7999, 22, true, true, "audio", "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1"),
                new ProductSeed("seed-bose-qc-ultra-earbuds", "Bose QC Ultra Earbuds", "Compact premium earbuds with immersive audio and strong noise cancellation.", 29900, 25900, 10, true, true, "audio", "https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46"),
                new ProductSeed("seed-sennheiser-accentum", "Sennheiser Accentum", "Wireless headphones with balanced sound, long battery life, and soft ear cushions.", 15990, 12990, 19, true, true, "audio", "https://images.unsplash.com/photo-1505740420928-5e560c06d30e"),
                new ProductSeed("seed-boat-airdopes-141", "boAt Airdopes 141", "Affordable true wireless earbuds with low-latency mode and pocketable charging case.", 4490, 1499, 45, true, true, "audio", "https://images.unsplash.com/photo-1590658268037-6bf12165a8df"),
                new ProductSeed("seed-marshall-emberton-ii", "Marshall Emberton II", "Rugged portable speaker with signature styling, rich audio, and long playback.", 21999, 18999, 12, true, true, "audio", "https://images.unsplash.com/photo-1545454675-3531b543be5d"),
                new ProductSeed("seed-galaxy-buds-fe", "Samsung Galaxy Buds FE", "Comfortable everyday earbuds with active noise cancellation and clear calls.", 9999, 6999, 26, true, true, "audio", "https://images.unsplash.com/photo-1572569511254-d8f925fe2cbb"),
                new ProductSeed("seed-soundcore-liberty-4-nc", "Soundcore Liberty 4 NC", "Feature-rich wireless earbuds with adaptive noise cancellation and app controls.", 8999, 6499, 24, true, true, "audio", "https://images.unsplash.com/photo-1610438235354-a6ae5528385c"),

                new ProductSeed("seed-lg-refrigerator", "LG Double Door Refrigerator", "260L frost-free refrigerator with smart inverter compressor.", 30000, 28000, 5, true, true, "home-appliances", "https://images.unsplash.com/photo-1571175443880-49e1d25b2bc5"),
                new ProductSeed("seed-washing-machine", "Samsung Front Load Washing Machine", "Energy-efficient front load washer with quick wash and digital inverter motor.", 42000, 37990, 7, true, true, "home-appliances", "https://images.unsplash.com/photo-1626806787461-102c1bfaaea1"),
                new ProductSeed("seed-ifb-microwave-25l", "IFB 25L Convection Microwave", "Convection microwave for baking, grilling, reheating, and everyday cooking.", 17990, 13990, 16, true, true, "home-appliances", "https://images.unsplash.com/photo-1574269909862-7e1d70bb8078"),
                new ProductSeed("seed-dyson-v8-vacuum", "Dyson V8 Vacuum Cleaner", "Cord-free vacuum cleaner with strong suction for floors, sofas, and car interiors.", 39900, 32900, 9, true, true, "home-appliances", "https://plus.unsplash.com/premium_photo-1677234148197-645f26ee2454"),
                new ProductSeed("seed-philips-air-fryer-xl", "Philips Air Fryer XL", "Large air fryer for crisp snacks and low-oil family cooking.", 18995, 14995, 18, true, true, "home-appliances", "https://plus.unsplash.com/premium_photo-1711051351678-658b273f71d4"),
                new ProductSeed("seed-voltas-15-ton-ac", "Voltas 1.5 Ton Inverter AC", "Energy-saving split AC with quick cooling and stabilizer-free operation.", 45990, 38990, 8, true, true, "home-appliances", "https://images.unsplash.com/photo-1762341123870-d706f257a12e"),
                new ProductSeed("seed-kent-grand-ro", "Kent Grand RO Water Purifier", "RO and UV water purifier with a transparent tank and household-friendly capacity.", 19990, 15990, 13, true, true, "home-appliances", "https://images.unsplash.com/photo-1662647343528-f7a5ed62c2dd"),
                new ProductSeed("seed-bajaj-mixer-grinder", "Bajaj Mixer Grinder", "Powerful kitchen mixer with stainless steel jars for grinding and blending.", 5499, 3499, 31, true, true, "home-appliances", "https://images.unsplash.com/photo-1570222094114-d054a817e56b"),

                new ProductSeed("seed-ps5", "PlayStation 5 Slim", "Next generation console with fast loading, 4K gaming, and DualSense controller.", 54990, 49990, 8, true, true, "gaming", "https://images.unsplash.com/photo-1606813907291-d86efa9b94db"),
                new ProductSeed("seed-xbox-series-s", "Xbox Series S", "Compact digital console for fast next-gen gaming and Game Pass access.", 39990, 32990, 11, true, true, "gaming", "https://images.unsplash.com/photo-1621259182978-fbf93132d53d"),
                new ProductSeed("seed-nintendo-switch-oled", "Nintendo Switch OLED", "Hybrid handheld console with a vivid OLED screen and detachable controllers.", 34990, 30990, 14, true, true, "gaming", "https://images.unsplash.com/photo-1578303512597-81e6cc155b3e"),
                new ProductSeed("seed-logitech-g29", "Logitech G29 Racing Wheel", "Force-feedback racing wheel with pedals for realistic driving games.", 39995, 31995, 7, true, true, "gaming", "https://images.unsplash.com/photo-1743649978995-c76212449e15"),
                new ProductSeed("seed-razer-blackwidow-v4", "Razer BlackWidow V4", "Mechanical gaming keyboard with RGB lighting and fast tactile switches.", 17999, 14999, 15, true, true, "gaming", "https://images.unsplash.com/photo-1541140532154-b024d705b90a"),
                new ProductSeed("seed-hyperx-cloud-iii", "HyperX Cloud III Headset", "Comfortable wired gaming headset with clear microphone and spatial audio support.", 9990, 8490, 21, true, true, "gaming", "https://images.unsplash.com/photo-1599669454699-248893623440"),
                new ProductSeed("seed-dualsense-controller", "Sony DualSense Controller", "Wireless PS5 controller with adaptive triggers and haptic feedback.", 6990, 5990, 24, true, true, "gaming", "https://images.unsplash.com/photo-1752833005527-38a858157afd"),
                new ProductSeed("seed-asus-rog-ally", "Asus ROG Ally", "Windows handheld gaming device with a sharp display and powerful portable graphics.", 69990, 59990, 6, true, true, "gaming", "https://dlcdnwebimgs.asus.com/gain/E70351F2-3579-45FB-95AC-4D41DA42EF2C"),

                new ProductSeed("seed-apple-watch", "Apple Watch Series 9", "Smartwatch with health tracking, notifications, workouts, and premium build.", 41900, 37900, 14, true, true, "accessories", "https://images.unsplash.com/photo-1434493789847-2f02dc6ca35d"),
                new ProductSeed("seed-keyboard", "Mechanical Keyboard Pro", "RGB mechanical keyboard with tactile switches for typing and gaming.", 7999, 6499, 20, true, true, "accessories", "https://images.unsplash.com/photo-1587829741301-dc798b83add3"),
                new ProductSeed("seed-anker-65w-gan", "Anker 65W GaN Charger", "Compact USB-C charger for phones, tablets, and ultrabooks with fast charging.", 4999, 3499, 34, true, true, "accessories", "https://images.unsplash.com/photo-1583863788434-e58a36330cf0"),
                new ProductSeed("seed-logitech-mx-master-3s", "Logitech MX Master 3S", "Ergonomic wireless mouse with quiet clicks, precise tracking, and multi-device flow.", 10995, 8995, 18, true, true, "accessories", "https://images.unsplash.com/photo-1527814050087-3793815479db"),
                new ProductSeed("seed-sandisk-extreme-ssd", "SanDisk Extreme SSD 1TB", "Pocket-sized rugged SSD for fast backups, creator files, and travel storage.", 13999, 9999, 22, true, true, "accessories", "https://images.unsplash.com/photo-1756836857608-16c9213c1d3c"),
                new ProductSeed("seed-samsung-power-bank", "Samsung 10000mAh Power Bank", "Slim portable power bank with USB-C charging for phones and earbuds.", 2999, 2199, 42, true, true, "accessories", "https://plus.unsplash.com/premium_photo-1761033366849-c50f4a15d4c6"),
                new ProductSeed("seed-belkin-usb-c-hub", "Belkin USB-C Hub", "Multiport hub with HDMI, USB-A, card reader, and laptop-friendly pass-through power.", 6999, 5499, 25, true, true, "accessories", "https://images.unsplash.com/photo-1760376789487-994070337c76"),
                new ProductSeed("seed-tplink-wifi-6-router", "TP-Link WiFi 6 Router", "Dual-band router for faster home networking, streaming, and work-from-home devices.", 8999, 6999, 19, true, true, "accessories", "https://images.unsplash.com/photo-1606904825846-647eb07f5be2"),

                new ProductSeed("seed-samsung-neo-qled-55", "Samsung Neo QLED 55", "Premium 4K Mini LED television with vivid HDR, smart apps, and smooth gaming.", 149990, 124990, 8, true, true, "televisions", "https://images.unsplash.com/photo-1593784991095-a205069470b6"),
                new ProductSeed("seed-lg-oled-c4-55", "LG OLED C4 55", "OLED television with perfect blacks, Dolby Vision, and a fast 120Hz gaming panel.", 179990, 149990, 6, true, true, "televisions", "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1"),
                new ProductSeed("seed-sony-bravia-7-55", "Sony Bravia 7 Mini LED 55", "Cinema-focused 4K Mini LED television with detailed contrast and immersive sound.", 189900, 164900, 5, true, true, "televisions", "https://images.unsplash.com/photo-1586024486164-ce9b3d87e09f"),
                new ProductSeed("seed-tcl-c755-qled-55", "TCL C755 QLED 55", "Bright QLED television with local dimming, Google TV, and high refresh rate support.", 89990, 74990, 12, true, true, "televisions", "https://images.unsplash.com/photo-1613280194169-6bb2f32a6bfa"),
                new ProductSeed("seed-xiaomi-x-pro-43", "Xiaomi Smart TV X Pro 43", "Affordable 4K smart television with Dolby Vision, Google TV, and slim bezels.", 49999, 39999, 18, true, true, "televisions", "https://images.unsplash.com/photo-1595935736128-db1f0a261263"),
                new ProductSeed("seed-hisense-u7k-55", "Hisense U7K 55", "Feature-rich Mini LED television with strong HDR brightness and gaming controls.", 79990, 64990, 10, true, true, "televisions", "https://images.unsplash.com/photo-1461151304267-38535e780c79"),
                new ProductSeed("seed-oneplus-y1s-pro-43", "OnePlus TV Y1S Pro 43", "Everyday 4K smart television with clear sound, HDR support, and useful casting.", 39999, 32999, 21, true, true, "televisions", "https://images.unsplash.com/photo-1540224871915-bc8ffb782bdf"),
                new ProductSeed("seed-acer-super-series-50", "Acer Super Series 50", "Value-focused 4K television with smart streaming, MEMC, and wide color support.", 44999, 36999, 15, true, true, "televisions", "https://images.unsplash.com/photo-1560169897-fc0cdbdfa4d5")
        );

        products.forEach(this::upsertProduct);
        mergeDuplicateCategories();
    }

    private void upsertCategory(CategorySeed seed) {
        Category category = categoryRepository.findById(seed.id()).orElseGet(Category::new);
        category.setCategoryId(seed.id());
        category.setTitle(seed.title());
        category.setDescription(seed.description());
        category.setCoverImage(seed.coverImage());
        categoryRepository.save(category);
    }

    private void upsertProduct(ProductSeed seed) {
        Product product = productRepository.findByTitleIgnoreCase(seed.title()).stream()
                .findFirst()
                .or(() -> productRepository.findById(seed.id()))
                .orElseGet(Product::new);

        if (product.getProductId() == null) {
            product.setProductId(seed.id());
            product.setAddedDate(LocalDateTime.now());
        } else if (product.getAddedDate() == null) {
            product.setAddedDate(LocalDateTime.now());
        }

        Category category = categoryRepository.findById(seed.categoryId()).orElseThrow();
        product.setTitle(seed.title());
        product.setDescription(seed.description());
        product.setPrice(seed.price());
        product.setDiscountedPrice(seed.discountedPrice());
        product.setQuantity(seed.quantity());
        product.setLive(seed.live());
        product.setStock(seed.stock());
        product.setProductImageName(seed.imageUrl());
        product.setCategory(category);
        productRepository.save(product);
    }

    private void mergeDuplicateCategories() {
        Map<String, List<Category>> categoriesByTitle = new LinkedHashMap<>();
        categoryRepository.findAll().forEach((category) ->
                categoriesByTitle.computeIfAbsent(normalizeTitle(category.getTitle()), ignored -> new ArrayList<>()).add(category)
        );

        categoriesByTitle.values().stream()
                .filter((sameTitleCategories) -> sameTitleCategories.size() > 1)
                .forEach(this::mergeCategoryGroup);
    }

    private void mergeCategoryGroup(List<Category> sameTitleCategories) {
        sameTitleCategories.sort(Comparator.comparing(Category::getCategoryId));
        Category keeper = sameTitleCategories.stream()
                .filter((category) -> category.getCategoryId().equals(slugify(category.getTitle())))
                .findFirst()
                .orElse(sameTitleCategories.get(0));

        for (Category duplicate : sameTitleCategories) {
            if (duplicate.getCategoryId().equals(keeper.getCategoryId())) {
                continue;
            }

            productRepository.findByCategory(duplicate).forEach((product) -> {
                product.setCategory(keeper);
                productRepository.save(product);
            });
            productRepository.flush();
            categoryRepository.delete(duplicate);
        }
    }

    private String normalizeTitle(String title) {
        return title == null ? "" : title.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    private String slugify(String title) {
        return normalizeTitle(title).replace(" ", "-");
    }

    private record CategorySeed(String id, String title, String description, String coverImage) {
    }

    private record ProductSeed(
            String id,
            String title,
            String description,
            int price,
            int discountedPrice,
            int quantity,
            boolean live,
            boolean stock,
            String categoryId,
            String imageUrl
    ) {
    }
}
