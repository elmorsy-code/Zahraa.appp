package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.network.supabase.SupabaseConfigManager
import com.example.network.supabase.SupabaseConnectionState
import com.example.ui.MainViewModel

@Composable
fun SupabaseConfigDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val status by viewModel.supabaseStatus.collectAsState()
    val isSyncing by viewModel.isSupabaseSyncing.collectAsState()

    val configManager = remember { SupabaseConfigManager.getInstance(context) }
    var urlInput by remember { mutableStateOf(configManager.getSupabaseUrl()) }
    var keyInput by remember { mutableStateOf(configManager.getSupabaseAnonKey()) }
    var isKeyVisible by remember { mutableStateOf(false) }
    var showSqlSection by remember { mutableStateOf(false) }

    val isConnected = status.state == SupabaseConnectionState.CONNECTED

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .clip(RoundedCornerShape(24.dp))
                    .testTag("supabase_config_dialog"),
                color = Color(0xFF0F172A),
                tonalElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Color(0xFF064E3B), RoundedCornerShape(12.dp))
                                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.Cloud,
                                    contentDescription = null,
                                    tint = if (isConnected) Color(0xFF34D399) else Color(0xFF94A3B8),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "الربط السحابي مع Supabase",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "قاعدة بيانات سحابية مركزية ومباشرة بدون Local Storage",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Badge Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (status.state) {
                                SupabaseConnectionState.CONNECTED -> Color(0xFF064E3B).copy(alpha = 0.6f)
                                SupabaseConnectionState.CONNECTING -> Color(0xFF1E293B)
                                SupabaseConnectionState.ERROR -> Color(0xFF7F1D1D).copy(alpha = 0.6f)
                                SupabaseConnectionState.DISCONNECTED -> Color(0xFF1E293B)
                            }
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when (status.state) {
                                SupabaseConnectionState.CONNECTED -> Color(0xFF10B981)
                                SupabaseConnectionState.CONNECTING -> Color(0xFF38BDF8)
                                SupabaseConnectionState.ERROR -> Color(0xFFEF4444)
                                SupabaseConnectionState.DISCONNECTED -> Color(0xFF475569)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (status.state) {
                                    SupabaseConnectionState.CONNECTED -> Icons.Default.CheckCircle
                                    SupabaseConnectionState.CONNECTING -> Icons.Default.Refresh
                                    SupabaseConnectionState.ERROR -> Icons.Default.Warning
                                    SupabaseConnectionState.DISCONNECTED -> Icons.Default.CloudOff
                                },
                                contentDescription = null,
                                tint = when (status.state) {
                                    SupabaseConnectionState.CONNECTED -> Color(0xFF34D399)
                                    SupabaseConnectionState.CONNECTING -> Color(0xFF38BDF8)
                                    SupabaseConnectionState.ERROR -> Color(0xFFF87171)
                                    SupabaseConnectionState.DISCONNECTED -> Color(0xFFCBD5E1)
                                },
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (status.state) {
                                        SupabaseConnectionState.CONNECTED -> "متصل بـ Supabase السحابي 🟢"
                                        SupabaseConnectionState.CONNECTING -> "جاري الاتصال والمزامنة..."
                                        SupabaseConnectionState.ERROR -> "خطأ في الاتصال 🔴"
                                        SupabaseConnectionState.DISCONNECTED -> "غير متصل - أدخل بيانات مشروعك 🟡"
                                    },
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = status.message.ifBlank { "أدخل رابط المشروع ومفتاح الوصول لبدء سحب وحفظ البيانات سحابياً مباشرة" },
                                    color = Color(0xFFCBD5E1),
                                    fontSize = 11.sp
                                )
                                if (isConnected) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "المزامنة الحالية: ${status.syncedCustomersCount} عميل • ${status.syncedProductsCount} صنف" +
                                                (if (status.lastSyncTime != null) " • آخر مزامنة: ${status.lastSyncTime}" else ""),
                                        color = Color(0xFF34D399),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Input Fields
                    Text(
                        text = "رابط المشروع السحابي (Supabase Project URL):",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supabase_url_input"),
                        placeholder = { Text("https://your-project.supabase.co", color = Color(0xFF64748B), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Link, contentDescription = null, tint = Color(0xFF38BDF8))
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "مفتاح الوصول العام (Anon Public Key):",
                        color = Color(0xFFCBD5E1),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("supabase_key_input"),
                        placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI...", color = Color(0xFF64748B), fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFF59E0B))
                        },
                        trailingIcon = {
                            IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                Icon(
                                    imageVector = if (isKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF1E293B),
                            unfocusedContainerColor = Color(0xFF1E293B)
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons
                    Button(
                        onClick = {
                            viewModel.testAndSaveSupabaseConfig(urlInput, keyInput) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("supabase_save_button"),
                        enabled = !isSyncing && urlInput.isNotBlank() && keyInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("جاري الاختبار والربط...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("اختبار وحفظ الاتصال بـ Supabase", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (isConnected) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Sync now button
                            OutlinedButton(
                                onClick = {
                                    viewModel.syncSupabase { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                enabled = !isSyncing,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("سحب من السحابة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Push seed button
                            OutlinedButton(
                                onClick = {
                                    viewModel.pushInitialSeedToSupabase { success, msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                enabled = !isSyncing,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF59E0B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD97706)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("رفع البيانات الشاملة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Toggle SQL Schema View
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFFA7F3D0), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أوامر SQL لإنشاء الجداول في Supabase",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        OutlinedButton(
                            onClick = { showSqlSection = !showSqlSection },
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981))
                        ) {
                            Text(if (showSqlSection) "إخفاء" else "عرض والنسخ", fontSize = 11.sp)
                        }
                    }

                    AnimatedVisibility(visible = showSqlSection) {
                        Column(modifier = Modifier.padding(top = 8.dp)) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF090D16)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("كود PostgreSQL لـ Supabase SQL Editor:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(SUPABASE_SQL_SCRIPT))
                                                Toast.makeText(context, "تم نسخ كود SQL إلى الحافظة!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    SelectionContainer {
                                        Text(
                                            text = SUPABASE_SQL_SCRIPT,
                                            color = Color(0xFF38BDF8),
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier
                                                .heightIn(max = 160.dp)
                                                .verticalScroll(rememberScrollState())
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Footer actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isConnected) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.disconnectSupabase()
                                    Toast.makeText(context, "تم فصل Supabase", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF991B1B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("فصل الاتصال", fontSize = 12.sp)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("إغلاق", color = Color.White, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

private const val SUPABASE_SQL_SCRIPT = """
-- إنشاء جداول منظومة صرح الزهراء في Supabase
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- 1. العملاء والمزارعين
CREATE TABLE IF NOT EXISTS customers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(50) UNIQUE NOT NULL,
  name VARCHAR(250) NOT NULL,
  partner_name VARCHAR(250),
  phone VARCHAR(50),
  village_or_address VARCHAR(250),
  credit_limit NUMERIC(15, 3) DEFAULT 50000.0,
  opening_balance NUMERIC(15, 3) DEFAULT 0.0,
  current_balance NUMERIC(15, 3) DEFAULT 0.0,
  notes TEXT,
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. الأصناف والأدوية الزراعية
CREATE TABLE IF NOT EXISTS products (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code VARCHAR(50) UNIQUE NOT NULL,
  barcode VARCHAR(100),
  name VARCHAR(250) NOT NULL,
  commercial_name VARCHAR(250),
  active_ingredient TEXT,
  manufacturer VARCHAR(200),
  base_unit_name VARCHAR(50) DEFAULT 'عبوة',
  default_purchase_price NUMERIC(15, 3) DEFAULT 0.0,
  default_sale_price NUMERIC(15, 3) DEFAULT 0.0,
  min_stock_alert NUMERIC(15, 3) DEFAULT 10.0,
  is_pesticide BOOLEAN DEFAULT FALSE,
  description TEXT,
  image_url TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW(),
  updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. كشوف حسابات المديونيات والسندات
CREATE TABLE IF NOT EXISTS customer_ledger_entries (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
  entry_date TIMESTAMPTZ DEFAULT NOW(),
  entry_type VARCHAR(50) NOT NULL,
  document_number VARCHAR(100),
  description TEXT,
  debit NUMERIC(15, 3) DEFAULT 0.0,
  credit NUMERIC(15, 3) DEFAULT 0.0,
  running_balance NUMERIC(15, 3) DEFAULT 0.0,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. حركة الخزينة والسيولة
CREATE TABLE IF NOT EXISTS cash_transactions (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  transaction_number VARCHAR(50) UNIQUE NOT NULL,
  transaction_date TIMESTAMPTZ DEFAULT NOW(),
  transaction_type VARCHAR(50) NOT NULL,
  amount NUMERIC(15, 3) NOT NULL,
  direction VARCHAR(10) NOT NULL,
  balance_after NUMERIC(15, 3) DEFAULT 0.0,
  party_name VARCHAR(250),
  description TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. المصروفات التشغيلية
CREATE TABLE IF NOT EXISTS expenses (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  expense_number VARCHAR(50) UNIQUE NOT NULL,
  category VARCHAR(100) NOT NULL,
  amount NUMERIC(15, 3) NOT NULL,
  expense_date TIMESTAMPTZ DEFAULT NOW(),
  paid_to VARCHAR(250),
  notes TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 6. الدفاتر القديمة الورقية
CREATE TABLE IF NOT EXISTS old_ledgers (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  book_name VARCHAR(150) NOT NULL,
  page_number VARCHAR(50),
  entry_date DATE DEFAULT NOW(),
  party_name VARCHAR(250) NOT NULL,
  phone VARCHAR(50),
  village VARCHAR(250),
  debit_amount NUMERIC(15, 3) DEFAULT 0.0,
  settled_amount NUMERIC(15, 3) DEFAULT 0.0,
  is_transferred_to_system BOOLEAN DEFAULT FALSE,
  notes TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);
"""
