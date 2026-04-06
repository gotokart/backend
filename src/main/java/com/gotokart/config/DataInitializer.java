package com.gotokart.config;

import com.gotokart.model.Category;
import com.gotokart.model.Product;
import com.gotokart.model.User;
import com.gotokart.repository.CategoryRepository;
import com.gotokart.repository.ProductRepository;
import com.gotokart.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository  productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository     userRepository;
    private final PasswordEncoder    passwordEncoder;

    @Override
    public void run(String... args) {
        // Always ensure admin user exists (safe to run on every startup)
        if (userRepository.findByEmail("admin@gotokart.com").isEmpty()) {
            User admin = new User();
            admin.setName("Admin");
            admin.setEmail("admin@gotokart.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            System.out.println("✅ Admin user created → admin@gotokart.com / admin123");
        }

        if (productRepository.count() > 0) return; // products already seeded

        Category electronics = save("Electronics");
        Category clothing    = save("Clothing");
        Category footwear    = save("Footwear");
        Category accessories = save("Accessories");
        Category beauty      = save("Beauty");
        Category sports      = save("Sports");
        Category home        = save("Home & Kitchen");
        Category books       = save("Books & Stationery");
        Category food        = save("Food & Grocery");
        Category toys        = save("Toys & Games");

        productRepository.saveAll(List.of(

            // ── ELECTRONICS (12) ──────────────────────────────────────
            product("Samsung Galaxy S24",        "6.2\" AMOLED, 8GB RAM, 256GB",      79999,  30, electronics),
            product("iPhone 15",                 "6.1\" Super Retina, A16 Bionic",    89999,  25, electronics),
            product("OnePlus 12",                "Snapdragon 8 Gen 3, 50MP camera",   64999,  40, electronics),
            product("MacBook Air M2",            "13.6\" Liquid Retina, 8GB, 256GB", 114999,  15, electronics),
            product("Dell Inspiron 15 Laptop",   "Intel i5, 8GB RAM, 512GB SSD",      55999,  20, electronics),
            product("Sony WH-1000XM5 Headphone", "Industry-leading noise cancelling", 29999,  50, electronics),
            product("Apple AirPods Pro 2",       "Active noise cancellation, H2 chip",24999,  45, electronics),
            product("Samsung 55\" 4K Smart TV",  "Crystal UHD, HDR10+, Tizen OS",     52999,  18, electronics),
            product("Logitech MX Master 3 Mouse","Advanced wireless, 7 buttons",       9999,  60, electronics),
            product("Mechanical Keyboard RGB",   "TKL layout, Blue switches, USB-C",   5999,  80, electronics),
            product("Canon EOS M50 Camera",      "24.1MP, 4K video, flip screen",     54999,  12, electronics),
            product("Mi 65W Fast Charger",       "GaN technology, dual port, USB-C",   1999, 150, electronics),

            // ── CLOTHING (12) ──────────────────────────────────────────
            product("Levi's 501 Original Jeans",   "Classic straight fit, blue denim",   3999,  80, clothing),
            product("Allen Solly Formal Shirt",    "Slim fit, full sleeve, cotton",       1799,  60, clothing),
            product("Nike Dri-FIT T-Shirt",        "Moisture-wicking, athletic fit",      1499,  90, clothing),
            product("Zara Floral Summer Dress",    "V-neck, midi length, floral print",   3499,  40, clothing),
            product("H&M Oversized Hoodie",        "Cotton blend, kangaroo pocket",       2499,  55, clothing),
            product("Peter England Polo T-Shirt",  "Pique cotton, ribbed collar",         1299,  70, clothing),
            product("Adidas Track Pants",          "3-stripe, moisture management",       1999,  65, clothing),
            product("Raymond Suit",                "3-piece, wool blend, formal",        12999,  20, clothing),
            product("W Women Kurta",               "Straight cut, ethnic prints",         1599,  50, clothing),
            product("Van Heusen Blazer",           "Regular fit, single button",          5999,  30, clothing),
            product("Jockey Innerwear Pack of 3",  "100% cotton, tagless comfort",         899, 120, clothing),
            product("Louis Philippe Trousers",     "Slim fit, formal, stretch fabric",    2999,  45, clothing),

            // ── FOOTWEAR (10) ──────────────────────────────────────────
            product("Nike Air Max 270",          "Foam heel unit, mesh upper",      8999,  35, footwear),
            product("Adidas Ultraboost 22",      "Boost midsole, Primeknit upper", 12999,  25, footwear),
            product("Bata Formal Leather Shoes", "Oxford style, genuine leather",   3499,  50, footwear),
            product("Red Tape Casual Sneaker",   "Lace-up, rubber sole, comfort",   2499,  60, footwear),
            product("Woodland Trekking Boots",   "Waterproof, ankle support",       4999,  40, footwear),
            product("Hush Puppies Loafer",       "Slip-on, memory foam insole",     3999,  45, footwear),
            product("Crocs Classic Clog",        "Lightweight, waterproof, casual", 3299,  55, footwear),
            product("Kito Flip Flop",            "EVA sole, anti-skid, comfortable",  499, 200, footwear),
            product("Puma Running Shoes",        "Running specific, air mesh upper", 4499,  35, footwear),
            product("Catwalk Heels",             "Block heel, suede finish, 3 inch", 2199,  30, footwear),

            // ── ACCESSORIES (10) ───────────────────────────────────────
            product("Fossil Gen 6 Smartwatch",     "Wear OS, heart rate, GPS",          19999,  20, accessories),
            product("Ray-Ban Aviator Sunglasses",  "Gold frame, green G-15 lens",        9999,  30, accessories),
            product("Wildcraft 30L Backpack",      "Laptop sleeve, water resistant",     2999,  55, accessories),
            product("Tommy Hilfiger Leather Belt", "Genuine leather, pin buckle",        2499,  40, accessories),
            product("Lavie Handbag",               "PU leather, multiple pockets",       1999,  35, accessories),
            product("Casio Digital Watch",         "Water resistant, stopwatch, alarm",  1499,  80, accessories),
            product("Fossil Leather Wallet",       "Bifold, 6 card slots, RFID block",   2999,  45, accessories),
            product("American Tourister Trolley",  "20\" cabin size, TSA lock",          6999,  25, accessories),
            product("Titan Raga Watch",            "Rose gold dial, leather strap",      8999,  20, accessories),
            product("Coach Clutch Bag",            "Crossgrain leather, zip closure",    5999,  15, accessories),

            // ── BEAUTY (10) ────────────────────────────────────────────
            product("Lakme 9to5 Foundation",    "SPF 40, long-wearing, 24HR coverage", 799,  60, beauty),
            product("Maybelline Fit Me Powder", "Controls oil, natural finish",          549,  80, beauty),
            product("L'Oreal Revitalift Serum", "1.5% pure hyaluronic acid",           1299,  50, beauty),
            product("Mamaearth Vitamin C Face Wash","Brightening, gentle, sulphate-free",299, 120, beauty),
            product("Forest Essentials Rose Water","Pure, hydrating, Ayurvedic toner",   895,  40, beauty),
            product("Biotique Bio Sunscreen SPF50","UVA/UVB protection, moisturising",   399,  90, beauty),
            product("Philips BT3231 Beard Trimmer","40 length settings, USB charge",    1999,  45, beauty),
            product("Head & Shoulders Shampoo 400ml","Anti-dandruff, 2-in-1 formula",    349, 100, beauty),
            product("Revlon Lipstick Super Lustrous","Moisturising, bold colours",        699,  70, beauty),
            product("Nivea Body Lotion 400ml",   "Deep moisture, 48HR care",             299, 110, beauty),

            // ── SPORTS (10) ────────────────────────────────────────────
            product("Cosco Cricket Bat",           "Kashmir willow, full size",        1299,  40, sports),
            product("Nivia Football",              "Size 5, machine stitched, PU",       999,  50, sports),
            product("Decathlon Yoga Mat",          "6mm thick, anti-slip surface",       999,  60, sports),
            product("Kore Adjustable Dumbbell 20kg","Home gym, rubber coated",          3999,  30, sports),
            product("Cosco Badminton Racket Set",  "2 rackets + 3 shuttlecocks",         799,  55, sports),
            product("Nivia Basketball",            "Rubber, textured grip, size 7",      899,  35, sports),
            product("Fitbit Inspire 3",            "24/7 heart rate, sleep tracking",  8999,  20, sports),
            product("Skipping Rope Ball Bearings", "Adjustable, foam handles",           399, 100, sports),
            product("MuscleTech Whey Protein 2kg", "25g protein per serving, chocolate", 3499,  25, sports),
            product("Strauss Resistance Band Set", "5 levels, latex, carry bag",         799,  70, sports),

            // ── HOME & KITCHEN (10) ────────────────────────────────────
            product("Prestige Induction Cooktop",  "2000W, auto-off, touch panel",     2999,  30, home),
            product("Philips Air Fryer 4.1L",      "Rapid Air tech, digital display",  8999,  20, home),
            product("Milton Thermosteel Flask 1L",  "24HR hot/cold, leak-proof",        1199,  80, home),
            product("Solimo Bed Sheet King Size",   "180TC cotton, 1 sheet + 2 pillow",  999,  60, home),
            product("Amazon Basics Pillow Pack of 2","Microfibre fill, soft touch",      799,  90, home),
            product("Bosch Hand Blender 450W",     "600ml jar, stainless blade",        2499,  35, home),
            product("Wipro LED Bulb 9W Pack of 6", "Cool white, energy saving",          549, 150, home),
            product("Cello Opalware Dinner Set 27pc","Microwave safe, chip resistant",  2999,  25, home),
            product("Asian Paints Wall Putty 10kg", "Smooth finish, crack resistance", 1199,  40, home),
            product("Pigeon Pressure Cooker 5L",   "Aluminium, safety valve, ISI mark", 1499,  50, home),

            // ── BOOKS & STATIONERY (10) ────────────────────────────────
            product("Atomic Habits – James Clear",   "Proven way to build good habits",   499,  80, books),
            product("The Alchemist – Paulo Coelho",  "International bestseller, fiction",  299,  90, books),
            product("Rich Dad Poor Dad",             "Personal finance classic",           399,  75, books),
            product("Class 10 NCERT Science Set",    "Complete set all chapters",          599,  60, books),
            product("Classmate 5-Subject Notebook",  "A4 size, 300 pages, hard cover",    199, 120, books),
            product("Parker Pen Gift Set",           "2 ball pens + 1 fountain pen",       799,  40, books),
            product("Staedtler Geometry Box",        "Metal compass, set square, ruler",   349,  70, books),
            product("Casio Scientific Calculator",   "FX-991EX, 552 functions",           1299,  50, books),
            product("Fevicol MR 250g",               "Water-based adhesive, strong bond",   99, 200, books),
            product("A4 Printer Paper 500 Sheets",   "75 GSM, bright white, acid-free",    449, 100, books),

            // ── FOOD & GROCERY (8) ─────────────────────────────────────
            product("Tata Tea Gold 500g",          "Premium Assam blend, strong taste",    299, 100, food),
            product("Nescafe Classic Coffee 200g", "100% pure coffee, rich aroma",         599,  80, food),
            product("Maggi Noodles Pack of 12",    "2-min noodles, masala flavour",        699, 120, food),
            product("Amul Butter 500g",            "Pasteurised, unsalted, fresh",         350,  90, food),
            product("Britannia Marie Gold Biscuit", "Light & crispy, 800g pack",           189, 150, food),
            product("Dabur Honey 1kg",             "100% pure, no sugar added",            599,  70, food),
            product("Fortune Sunflower Oil 5L",    "Light, healthy cooking oil",           799,  50, food),
            product("Cadbury Dairy Milk 162g",     "Smooth milk chocolate, gift pack",     199, 200, food),

            // ── TOYS & GAMES (10) ──────────────────────────────────────
            product("LEGO Classic Brick Box 484pc", "Colourful, open-ended play",        4999,  20, toys),
            product("Funskool Scrabble",            "Classic word game, 2-4 players",    1499,  35, toys),
            product("Monopoly Classic Board Game",  "Property trading, 2-6 players",     1999,  30, toys),
            product("Hot Wheels 20-Car Gift Pack",  "Die-cast cars, 1:64 scale",         1299,  45, toys),
            product("Fisher-Price Baby Gym",        "Soft, sensory, 0-6 months",         2999,  20, toys),
            product("Rubik's Cube 3x3",             "Original, smooth turning",            699,  80, toys),
            product("Remote Control Car",           "1:16 scale, 2.4GHz, 30 kmph",      1999,  30, toys),
            product("Uno Card Game",                "Classic family card game, 108 cards", 349, 100, toys),
            product("Carrom Board Full Size",       "Plywood, striker + coins included", 2499,  25, toys),
            product("Nerf Elite 2.0 Blaster",       "20 darts, rotating barrel",         1999,  30, toys)
        ));

        System.out.println("✅ Seeded " + productRepository.count() + " products successfully.");
    }

    private Category save(String name) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> {
                    Category c = new Category();
                    c.setName(name);
                    return categoryRepository.save(c);
                });
    }

    private Product product(String name, String desc, double price, int stock, Category category) {
        Product p = new Product();
        p.setName(name);
        p.setDescription(desc);
        p.setPrice(price);
        p.setStock(stock);
        p.setCategory(category);
        return p;
    }
}
