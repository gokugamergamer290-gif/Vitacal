package com.example.data.catalog

import com.example.data.model.CatalogFood

object BrazilianFoodCatalog {
  val categories = listOf(
    "Todos",
    "Proteínas",
    "Carboidratos",
    "Frutas",
    "Vegetais",
    "Laticínios",
    "Gorduras",
    "Bebidas",
    "Meus Alimentos",
  )

  val builtInFoods: List<CatalogFood> = listOf(
    // Proteínas
    CatalogFood("f_frango", "Peito de frango grelhado", "Proteínas", "100 g", 165, 31.0, 0.0, 3.6),
    CatalogFood("f_ovo_mexido", "Ovo mexido / cozido", "Proteínas", "1 unidade (50 g)", 78, 6.3, 0.6, 5.3),
    CatalogFood("f_clara", "Clara de ovo cozida", "Proteínas", "2 unidades (66 g)", 34, 7.2, 0.4, 0.1),
    CatalogFood("f_patinho", "Patinho moído grelhado", "Proteínas", "100 g", 198, 29.0, 0.0, 8.5),
    CatalogFood("f_salmao", "Salmão grelhado", "Proteínas", "100 g", 208, 20.4, 0.0, 13.4),
    CatalogFood("f_tilapia", "Filé de tilápia grelhado", "Proteínas", "100 g", 128, 26.2, 0.0, 2.7),
    CatalogFood("f_atum", "Atum em água (escorrido)", "Proteínas", "1 lata (120 g)", 132, 28.0, 0.0, 1.2),
    CatalogFood("f_whey", "Whey protein concentrado", "Proteínas", "1 scoop (30 g)", 120, 24.0, 2.5, 1.5),
    CatalogFood("f_tofu", "Tofu firme", "Proteínas", "100 g", 76, 8.1, 1.9, 4.8),
    CatalogFood("f_lombo", "Lombo suíno assado", "Proteínas", "100 g", 175, 27.0, 0.0, 6.8),

    // Carboidratos
    CatalogFood("f_arroz_branco", "Arroz branco cozido", "Carboidratos", "100 g (4 col. sopa)", 128, 2.5, 28.1, 0.2),
    CatalogFood("f_arroz_integral", "Arroz integral cozido", "Carboidratos", "100 g (4 col. sopa)", 124, 2.6, 25.8, 1.0),
    CatalogFood("f_feijao_carioca", "Feijão carioca cozido", "Carboidratos", "1 concha (100 g)", 76, 4.8, 13.6, 0.5),
    CatalogFood("f_feijao_preto", "Feijão preto cozido", "Carboidratos", "1 concha (100 g)", 77, 4.5, 14.0, 0.5),
    CatalogFood("f_batata_doce", "Batata-doce cozida", "Carboidratos", "100 g", 77, 0.6, 18.4, 0.1),
    CatalogFood("f_batata_inglesa", "Batata inglesa cozida", "Carboidratos", "100 g", 52, 1.2, 11.9, 0.0),
    CatalogFood("f_mandioca", "Mandioca / aipim cozido", "Carboidratos", "100 g", 125, 0.6, 30.1, 0.3),
    CatalogFood("f_aveia", "Aveia em flocos", "Carboidratos", "30 g (2 col. sopa)", 117, 4.2, 20.0, 2.2),
    CatalogFood("f_tapioca", "Goma de tapioca hidratada", "Carboidratos", "50 g (3 col. sopa)", 120, 0.0, 30.0, 0.0),
    CatalogFood("f_pao_frances", "Pão francês tradicional", "Carboidratos", "1 unidade (50 g)", 150, 4.0, 29.3, 1.6),
    CatalogFood("f_pao_integral", "Pão de forma integral", "Carboidratos", "2 fatias (50 g)", 126, 5.2, 22.0, 1.8),
    CatalogFood("f_cuscuz", "Cuscuz de milho cozido", "Carboidratos", "100 g", 113, 2.2, 25.3, 0.7),
    CatalogFood("f_macarrao", "Macarrão cozido", "Carboidratos", "100 g", 131, 4.5, 26.0, 0.9),
    CatalogFood("f_granola", "Granola tradicional", "Carboidratos", "40 g", 168, 4.0, 28.0, 4.4),

    // Frutas
    CatalogFood("f_banana", "Banana prata", "Frutas", "1 unidade (86 g)", 84, 1.1, 21.8, 0.1),
    CatalogFood("f_maca", "Maçã gala", "Frutas", "1 unidade média (130 g)", 72, 0.3, 19.0, 0.2),
    CatalogFood("f_mamao", "Mamão papaia", "Frutas", "1/2 unidade (150 g)", 60, 0.8, 15.6, 0.2),
    CatalogFood("f_abacaxi", "Abacaxi em cubos", "Frutas", "100 g (1 fatia)", 48, 0.9, 12.3, 0.1),
    CatalogFood("f_morango", "Morangos frescos", "Frutas", "100 g (8 unid.)", 30, 0.9, 6.8, 0.3),
    CatalogFood("f_manga", "Manga palmer", "Frutas", "100 g", 72, 0.4, 19.4, 0.2),
    CatalogFood("f_laranja", "Laranja pera", "Frutas", "1 unidade (140 g)", 62, 1.2, 15.0, 0.2),
    CatalogFood("f_melancia", "Melancia em cubos", "Frutas", "150 g (1 fatia)", 49, 1.3, 12.1, 0.0),

    // Vegetais
    CatalogFood("f_brocolis", "Brócolis cozido", "Vegetais", "100 g", 25, 2.1, 4.4, 0.5),
    CatalogFood("f_salada", "Salada de alface e tomate", "Vegetais", "1 prato (100 g)", 18, 1.1, 3.4, 0.2),
    CatalogFood("f_cenoura", "Cenoura cozida", "Vegetais", "100 g", 30, 0.8, 6.7, 0.2),
    CatalogFood("f_abobrinha", "Abobrinha refogada", "Vegetais", "100 g", 24, 1.2, 4.3, 0.4),
    CatalogFood("f_couve", "Couve refogada", "Vegetais", "80 g (2 col. servir)", 46, 2.1, 5.2, 2.0),

    // Laticínios
    CatalogFood("f_iogurte", "Iogurte natural desnatado", "Laticínios", "1 pote (170 g)", 75, 7.0, 10.2, 0.5),
    CatalogFood("f_queijo_minas", "Queijo minas frescal", "Laticínios", "1 fatia média (40 g)", 106, 7.0, 1.3, 8.1),
    CatalogFood("f_requeijao", "Requeijão cremoso light", "Laticínios", "1 col. sopa (30 g)", 52, 3.2, 1.1, 3.9),
    CatalogFood("f_leite_desnatado", "Leite desnatado", "Laticínios", "1 copo (200 ml)", 68, 6.4, 9.6, 0.4),
    CatalogFood("f_leite_integral", "Leite integral", "Laticínios", "1 copo (200 ml)", 120, 6.2, 9.4, 6.4),
    CatalogFood("f_mussarela", "Queijo muçarela", "Laticínios", "2 fatias (30 g)", 99, 6.8, 0.9, 7.5),

    // Gorduras
    CatalogFood("f_azeite", "Azeite de oliva extra virgem", "Gorduras", "1 col. sopa (10 ml)", 88, 0.0, 0.0, 10.0),
    CatalogFood("f_abacate", "Abacate", "Gorduras", "100 g (3 col. sopa)", 96, 1.2, 6.0, 8.4),
    CatalogFood("f_pasta_amendoim", "Pasta de amendoim integral", "Gorduras", "1 col. sopa (15 g)", 90, 4.2, 2.8, 7.5),
    CatalogFood("f_castanha", "Castanha-do-pará", "Gorduras", "3 unidades (12 g)", 78, 1.7, 1.4, 7.8),
    CatalogFood("f_chia", "Semente de chia", "Gorduras", "1 col. sopa (15 g)", 73, 2.5, 6.3, 4.6),

    // Bebidas
    CatalogFood("f_cafe_preto", "Café preto sem açúcar", "Bebidas", "1 xícara (100 ml)", 2, 0.2, 0.3, 0.0),
    CatalogFood("f_cafe_leite", "Café com leite sem açúcar", "Bebidas", "1 xícara (200 ml)", 65, 3.4, 4.8, 3.5),
    CatalogFood("f_suco_laranja", "Suco de laranja natural", "Bebidas", "1 copo (250 ml)", 112, 1.7, 25.8, 0.5),
    CatalogFood("f_agua_coco", "Água de coco natural", "Bebidas", "1 copo (250 ml)", 48, 0.5, 11.5, 0.0),
    CatalogFood("f_vitamina_banana", "Vitamina de banana com aveia", "Bebidas", "1 copo (300 ml)", 235, 8.5, 41.0, 4.2),
  )
}
