package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.Product
import com.example.ui.MainViewModel
import com.example.ui.ZahraaTab
import java.util.Locale

data class WebSlide(
    val url: String,
    val caption: String,
    val subtitle: String,
    val tag: String
)

data class TeamMemberData(
    val name: String,
    val role: String,
    val department: String,
    val experience: String,
    val bio: String,
    val photoUrl: String,
    val phone: String
)

@Composable
fun WebHomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val products by viewModel.products.collectAsState()

    // Curated slides from the website's homeContent.ts
    val slides = listOf(
        WebSlide(
            url = "https://images.unsplash.com/photo-1585320806297-9794b3e4eeae?auto=format&fit=crop&w=1200&q=80",
            caption = "صرح الزهراء للأدوية والمبيدات الزراعية المعتمدة",
            subtitle = "أقوى المبيدات الحشرية والفطرية والنيماتودية المرخصة رسمياً لحماية تامة لمحصولك الزراعي",
            tag = "مبيدات زراعية مسجلة 100%"
        ),
        WebSlide(
            url = "https://images.unsplash.com/photo-1500651230702-0e2d8a49d4ad?auto=format&fit=crop&w=1200&q=80",
            caption = "أسمدة ومغذيات ورقية وأرضية فائقة الفاعلية",
            subtitle = "مركبات NPK، أحماض أمينية، منشطات جذور، وعناصر صغرى وكبرى لمضاعفة إنتاجية الفدان",
            tag = "أسمدة ومخصبات تخصصية"
        ),
        WebSlide(
            url = "https://images.unsplash.com/photo-1574943320219-553eb213f72d?auto=format&fit=crop&w=1200&q=80",
            caption = "بذور وتقاوي هجين منتقاة بأعلى نسب إنبات",
            subtitle = "بذور خضار ومحاصيل حقلية هجين معتمدة ومقاومة للأمراض والفيروسات والإجهادات المناخية",
            tag = "بذور وتقاوي هجين معتمدة"
        ),
        WebSlide(
            url = "https://images.unsplash.com/photo-1592417817098-8f3d69109853?auto=format&fit=crop&w=1200&q=80",
            caption = "استشارات صيدلانية زراعية وروشتات علاجية مجانية",
            subtitle = "فريق متخصص من خبراء وقاية النبات وتغذية المزروعات لتشخيص الآفات ووضع برامج التسميد والمكافحة",
            tag = "إشراف واستشارات زراعية"
        )
    )

    val teamMembers = listOf(
        TeamMemberData(
            name = "م. أحمد الزهراء",
            role = "المدير العام واستشاري تغذية النبات",
            department = "الإدارة العليا والاستشارات",
            experience = "خبرة 18 عاماً",
            bio = "خبير واستشاري تغذية المحاصيل وبرامج التسميد الحديث، خبير معتمد في كفاءة امتصاص العناصر الكبرى والصغرى.",
            photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
            phone = "+20 100 234 5678"
        ),
        TeamMemberData(
            name = "د. محمود الشريف",
            role = "رئيس قسم وقاية النبات والمكافحة",
            department = "الأدوية والمبيدات الزراعية",
            experience = "خبرة 14 عاماً",
            bio = "دكتوراه في أمراض النبات والمبيدات، متخصص في وضع برامج المكافحة المتكاملة (IPM) للآفات الحشرية والفطرية.",
            photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
            phone = "+20 101 345 6789"
        ),
        TeamMemberData(
            name = "م. ياسمين النجار",
            role = "أخصائية التقاوي وبذور الخضر",
            department = "البذور والتقاوي الهجين",
            experience = "خبرة 10 أعوام",
            bio = "مهندسة زراعية متخصصة في أصناف البذور الهجين واختبارات حيوية الإنبات ومقاومة الأمراض الوراثية.",
            photoUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80",
            phone = "+20 102 456 7890"
        ),
        TeamMemberData(
            name = "أ. طارق عبد الرحمن",
            role = "مدير العمليات وسلاسل التوريد",
            department = "التوزيع والشحن الزراعي",
            experience = "خبرة 12 عاماً",
            bio = "مسؤول عن تأمين وتوزيع الأدوية والأسمدة لكبرى المزارع والجمعيات والموزعين بكافة محافظات مصر.",
            photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80",
            phone = "+20 103 567 8901"
        )
    )

    var currentSlideIdx by remember { mutableIntStateOf(0) }
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }

    val categories = listOf(
        "الكل",
        "أدوية ومبيدات فطرية وحشرية",
        "أسمدة ومغذيات ورقية وأرضية",
        "بذور وتقاوي هجين معتمدة",
        "منشطات جذور ومخصبات حيوية",
        "مبيدات حشائش ونيماتودا ومواد ناشرة"
    )

    val filteredProducts = if (selectedCategoryFilter == "الكل") {
        products
    } else {
        products.filter { it.categoryName == selectedCategoryFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =========================================================================
        // 1. GRAND HERO SECTION (Matching website HeroSection.tsx)
        // =========================================================================
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("web_hero_card"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF041410)),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Top Announcement Strip
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF064E3B).copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34D399))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "عروض الموسم: مبيدات معتمدة، أسمدة مركبة NPK، وبذور هجين عالية الإنتاجية",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA7F3D0),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tagline Kicker
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF059669).copy(alpha = 0.25f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Spa, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "صرح الزهراء للأدوية والمبيدات الزراعية · منذ 2008",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFA7F3D0),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Grand Title
                    Text(
                        text = "صيدليتك الزراعية المعتمدة لحماية المحاصيل ومضاعفة الإنتاج",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        lineHeight = 32.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "أقوى المبيدات والأسمدة التخصصية والتقاوي الهجين",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "أكثر من 18 عاماً من الخبرة والريادة في توفير الأدوية الزراعية المرخصة، الأسمدة التخصصية، والمخصبات، والتقاوي الهجين، مع برامج مكافحة وتسميد متكاملة بإشراف نخبة من كبار الخبراء الزراعيين.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4 Trust Pillars Grid
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أدوية ومبيدات زراعية مرخصة رسمياً ومضمونة 100%", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("أسمدة تخصصية (NPK، أحماض، عناصر صغرى مخلبية)", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF34D399), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("بذور وتقاوي هجين معتمدة بأعلى نسب إنبات", style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // CTA Buttons Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.selectTab(ZahraaTab.PRODUCTS) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                        ) {
                            Text("دليل الأدوية", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { viewModel.selectTab(ZahraaTab.SIMSIM) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اسأل سمسم", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Live Metrics 4 Cards Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("+2,500", fontWeight = FontWeight.Black, color = Color(0xFF34D399), fontSize = 14.sp)
                                Text("صنف مسجل", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("18+", fontWeight = FontWeight.Black, color = Color(0xFF34D399), fontSize = 14.sp)
                                Text("عاماً خبرة", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("99.8%", fontWeight = FontWeight.Black, color = Color(0xFF34D399), fontSize = 14.sp)
                                Text("نجاح مكافحة", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text("100%", fontWeight = FontWeight.Black, color = Color(0xFF34D399), fontSize = 14.sp)
                                Text("أصلي معتمد", style = MaterialTheme.typography.labelSmall, color = Color.White, fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 2. ULTRA-PROFESSIONAL IMAGE SLIDER (Matching HomeSlider in HomeSections)
        // =========================================================================
        item {
            val slide = slides[currentSlideIdx]

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = slide.url,
                        contentDescription = slide.caption,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Cinematic Gradient Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF0F172A).copy(alpha = 0.95f)),
                                    startY = 100f
                                )
                            )
                    )

                    // Top Slide Counter & Tag
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${currentSlideIdx + 1} / ${slides.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF059669).copy(alpha = 0.8f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = slide.tag,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Bottom Content: Caption & Navigation Arrows
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = slide.caption,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = slide.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1),
                            maxLines = 2,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Controls: Dots and Prev/Next
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dots
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                slides.indices.forEach { i ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (i == currentSlideIdx) 18.dp else 6.dp, 6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(if (i == currentSlideIdx) Color(0xFF34D399) else Color.White.copy(alpha = 0.5f))
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .clickable {
                                            currentSlideIdx = (currentSlideIdx - 1 + slides.size) % slides.size
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ChevronRight, contentDescription = "السابق", tint = Color.White, modifier = Modifier.size(18.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f))
                                        .clickable {
                                            currentSlideIdx = (currentSlideIdx + 1) % slides.size
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ChevronLeft, contentDescription = "التالي", tint = Color.White, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // 3. VISION SECTION (رؤية الموقع - 4 ركائز)
        // =========================================================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "رؤية الصرح الزراعي",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "أهم الركائز والخدمات التي يقدمها صرح الزهراء لعملائنا الكرام",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                val pillars = listOf(
                    "توفير أدوية ومبيدات زراعية مسجلة ومطابقة للمواصفات بنسبة 100%",
                    "تقديم استشارات صيدلانية زراعية وبرامج تسميد ومكافحة دورية مجانية للمزارع",
                    "توفير أجود سلالات التقاوي والبذور الهجين ذات الإنبات العالي والمقاومة للأمراض",
                    "شحن وتوزيع مباشر وسريع للأسمدة والمستلزمات لكافة محافظات الجمهورية"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pillars.take(2).forEachIndexed { i, p ->
                        VisionMiniCard(number = i + 1, text = p, modifier = Modifier.weight(1f))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pillars.drop(2).forEachIndexed { i, p ->
                        VisionMiniCard(number = i + 3, text = p, modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // =========================================================================
        // 4. TEAM SECTION (الكفاءات وفريق العمل)
        // =========================================================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "فريق العمل والخبراء الزراعيين",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "نخبة متخصصة من مهندسي وقاية النبات وتغذية المزروعات",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                teamMembers.forEach { member ->
                    WebTeamCard(member = member)
                }
            }
        }

        // =========================================================================
        // 5. FEATURED PRODUCTS SHOWCASE (معرض الأدوية الحية)
        // =========================================================================
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "معرض الأدوية والأسمدة والبذور",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "بيانات حية ومباشرة من قاعدة بيانات المحل والمخازن",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "عرض الكل",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.selectTab(ZahraaTab.PRODUCTS) }
                    )
                }

                // Category Switcher Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }

        // Showcase Products List
        items(filteredProducts.take(6)) { prod ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectTab(ZahraaTab.PRODUCTS) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFD1FAE5))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = prod.categoryName,
                                    color = Color(0xFF047857),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = prod.code,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = prod.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black
                        )
                        if (!prod.activeIngredient.isNullOrBlank()) {
                            Text(
                                text = "المادة: ${prod.activeIngredient}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF0D9488)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${String.format(Locale.US, "%,.2f", prod.defaultSalePrice)} ج.م",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF047857),
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "المخزون: ${prod.totalStock}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VisionMiniCard(
    number: Int,
    text: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD1FAE5)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$number",
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF047857),
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF1E293B),
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun WebTeamCard(member: TeamMemberData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Top Green Gradient Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF064E3B), Color(0xFF10B981))
                        )
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = member.experience,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF34D399))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("متواجد", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Member Info Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = member.photoUrl,
                    contentDescription = member.name,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = member.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = member.role,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF047857),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = member.bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
