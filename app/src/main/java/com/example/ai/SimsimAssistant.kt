package com.example.ai

import com.example.model.SimsimMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object SimsimAssistant {

    val SYSTEM_INSTRUCTION = """
        أنت "سمسم" (Simsim)، المساعد والذكاء الاصطناعي الذكي والشامل لموقع ومنظومة "الزهراء للأدوية والمبيدات الزراعية".
        أنت ذكي، سريع البديهة، خفيف الظل، متجاوب، تتحدث بلهجة مصرية مهذبة ومحترفة.
        لديك تحكم كامل في وظائف وشاشات المنظومة وقدرة على توجيه المستخدم والإجابة عن الأسئلة الزراعية والمحاسبية وإدارة الحسابات والعملاء.
    """.trimIndent()

    data class LiveContext(
        val totalProducts: Int,
        val totalCash: Double,
        val totalCustomerDebt: Double,
        val totalSupplierDebt: Double,
        val lowStockCount: Int
    )

    fun resolveSmartResponse(userMessage: String, context: LiveContext): SimsimMessage {
        val lower = userMessage.trim().lowercase(Locale.ROOT)
        var actionTarget: String? = null
        var actionLabel: String? = null
        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val reply = when {
            lower.contains("سلام") || lower.contains("ازيك") || lower.contains("صباح") || lower.contains("مساء") || lower.contains("مين انت") -> {
                "وعليكم السلام ورحمة الله وبركاته! أنا \"سمسم\" رفيقك الذكي ومساعدك المتكامل لمنظومة الزهراء للأدوية والمبيدات الزراعية 🌿. أقدر أفتح لك أي شاشة، أراجع الخزينة وديون العملاء، وأجاوبك على أي استشارة زراعية أو مبيدات."
            }
            lower.contains("خزين") || lower.contains("الخزنة") || lower.contains("فلوس") || lower.contains("كاش") || lower.contains("سيول") -> {
                actionTarget = "treasury"
                actionLabel = "فتح قسم الخزينة والمصروفات"
                "فتحت لك شاشة الخزينة والمصروفات يا فندم! رصيد السيولة النقدية والبنكية الحالي المسجل بالخزائن: ${String.format(Locale.US, "%,.2f", context.totalCash)} ج.م."
            }
            lower.contains("ديون") || lower.contains("مديون") || lower.contains("باقي عليهم") || lower.contains("حسابات العملاء") -> {
                actionTarget = "debts"
                actionLabel = "فتح قسم مديونيات العملاء"
                "فتحت لك قسم مديونيات العملاء يا فندم. إجمالي الديون المستحقة على المزارعين حالياً: ${String.format(Locale.US, "%,.2f", context.totalCustomerDebt)} ج.م."
            }
            lower.contains("عملاء") || lower.contains("مزارع") || lower.contains("عميل") -> {
                actionTarget = "customers"
                actionLabel = "فتح دليل العملاء والمزارعين"
                "عيني ليك يا فندم! فتحت لك دليل المزارعين والعملاء للبحث الفوري ومتابعة أرقام الهواتف والقرى."
            }
            lower.contains("ضيف دواء") || lower.contains("دواء جديد") || lower.contains("صنف جديد") || lower.contains("اضف دواء") -> {
                actionTarget = "open_add_product"
                actionLabel = "فتح نافذة إضافة دواء جديد"
                "فتحت لك نافذة إضافة دواء زراعي جديد دلوقتي، تقدر ترفع صورته وتملى اسمه والمادة الفعالة والوزن والسعر."
            }
            lower.contains("ضيف دين") || lower.contains("سجل مديونية") || lower.contains("تسجيل شراء") || lower.contains("اخذ دواء") -> {
                actionTarget = "open_add_debt"
                actionLabel = "فتح نافذة تسجيل مديونية جديدة"
                "فتحت لك نافذة تسجيل مديونية جديدة! تقدر تختار المزارع وتحدد الأدوية سواء عبوة كاملة أو كمية فرعية (تجزئة كسر عبوة)."
            }
            lower.contains("مصروف") || lower.contains("تسجيل مصروف") || lower.contains("صرف") -> {
                actionTarget = "open_add_expense"
                actionLabel = "فتح نافذة تسجيل مصروف"
                "فتحت لك نافذة تسجيل مصروف تشغيلي جديد لخصمه من الخزينة المحددة."
            }
            lower.contains("منتجات") || lower.contains("أصناف") || lower.contains("أدوية") || lower.contains("مبيدات") || lower.contains("اسعار") -> {
                actionTarget = "products"
                actionLabel = "فتح دليل الأدوية والأصناف"
                "فتحت لك دليل الأدوية والمبيدات والأسمدة. إجمالي الأصناف المسجلة بالصيدلية والمخازن: ${context.totalProducts} صنف."
            }
            lower.contains("فاتورة") || lower.contains("فواتير") || lower.contains("مشتريات") || lower.contains("مورد") -> {
                actionTarget = "invoices"
                actionLabel = "فتح قسم الفواتير والموردين"
                "عيني ليك يا فندم! فتحت لك قسم فواتير المشتريات وحسابات الشركات والموردين الزراعيين."
            }
            lower.contains("دفاتر") || lower.contains("دفتر قديم") || lower.contains("ترحيل") -> {
                actionTarget = "old_ledgers"
                actionLabel = "فتح ترحيل الدفاتر الورقية"
                "فتحت لك شاشة الدفاتر الورقية القديمة مع إمكانية الترحيل المباشر لحساب العميل بنقرة واحدة."
            }
            lower.contains("ندوة") || lower.contains("بياض") || lower.contains("حشد") || lower.contains("دودة") || lower.contains("علاج") || lower.contains("بطاطس") || lower.contains("طماطم") || lower.contains("قمح") || lower.contains("تسميد") -> {
                "بخصوص الاستشارة الزراعية يا فندم: يُنصح بالفحص وتحديد طور الإصابة:\n" +
                "1. للأمراض الفطرية (ندوة مبكرة/متأخرة، بياض دقيقي): استخدم رادوميل جولد بلس بمعدل 250جم/100 لتر ماء أو سكور 250 EC.\n" +
                "2. لدودة الحشد الخريفية وتوتا أبسلوتا: استخدم كوراجين 20% SC بمعدل 60 مل/فدان أو أباكتين 5% سوبر.\n" +
                "3. لتنشيط الجذور والتزهير: استخدم روتكس ألترا وحقن هيوميك أسيد بلاتينيوم 85% مع ماء الري.\n" +
                "مع مراعاة فترة الأمان قبل الحصاد (PHI) والرش في الصباح الباكر أو بعد العصر."
            }
            else -> {
                "أمرك يا فندم! أنا سمسم المساعد الذكي لصرح الزهراء للأدوية والمبيدات الزراعية. تحب نراجع حسابات وديون المزارعين، نسجل مديونية جديدة، نفحص الخزينة، أو محتاج استشارة في محصول معين؟"
            }
        }

        return SimsimMessage(
            id = "msg_" + UUID.randomUUID().toString().take(8),
            sender = "simsim",
            text = reply,
            timestamp = timeStr,
            actionTarget = actionTarget,
            actionLabel = actionLabel
        )
    }
}
