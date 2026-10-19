package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.model.Product
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(
    productToEdit: Product?,
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(productToEdit?.name ?: "") }
    var commercialName by remember { mutableStateOf(productToEdit?.commercialName ?: "") }
    var activeIngredient by remember { mutableStateOf(productToEdit?.activeIngredient ?: "") }

    val categories = listOf(
        "أدوية ومبيدات فطرية وحشرية",
        "أسمدة ومغذيات ورقية وأرضية",
        "بذور وتقاوي هجين معتمدة",
        "منشطات جذور ومخصبات حيوية",
        "مبيدات حشائش ونيماتودا ومواد ناشرة"
    )
    var categoryName by remember { mutableStateOf(productToEdit?.categoryName ?: categories.first()) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var weightOrSize by remember { mutableStateOf(productToEdit?.weightOrSize ?: "عبوة 250 مل") }
    var salePrice by remember { mutableStateOf(productToEdit?.defaultSalePrice?.toString() ?: "") }
    var purchasePrice by remember { mutableStateOf(productToEdit?.defaultPurchasePrice?.toString() ?: "") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0D9488))
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (productToEdit != null) "تعديل بيانات الدواء" else "إضافة دواء أو مبيد جديد",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "الاسم والمادة الفعالة والتصنيف والوزن وسعر البيع",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFCCFBF1)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إلغاء", tint = Color.White)
                    }
                }

                // Scrollable Inputs Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم الدواء / الصنف *") },
                        placeholder = { Text("مثال: أباكتين 5% سوبر") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = activeIngredient,
                        onValueChange = { activeIngredient = it },
                        label = { Text("المادة الفعالة والتركيز") },
                        placeholder = { Text("مثال: Abamectin 5%") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Category Selector
                    ExposedDropdownMenuBox(
                        expanded = categoryDropdownExpanded,
                        onExpandedChange = { categoryDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = categoryName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("تصنيف الدواء") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        categoryName = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = weightOrSize,
                        onValueChange = { weightOrSize = it },
                        label = { Text("الوزن أو الحجم") },
                        placeholder = { Text("مثال: عبوة 250 مل، كيس 1 كجم...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = salePrice,
                            onValueChange = { salePrice = it },
                            label = { Text("سعر البيع (ج.م) *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = purchasePrice,
                            onValueChange = { purchasePrice = it },
                            label = { Text("سعر الشراء (اختياري)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("إرشادات وتأثير الدواء (اختياري)") },
                        placeholder = { Text("مكافحة ديدان الثمار، البياض الزغبي...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = Color.Gray, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val sp = salePrice.toDoubleOrNull() ?: 0.0
                            val pp = purchasePrice.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank()) {
                                viewModel.saveProduct(
                                    id = productToEdit?.id,
                                    name = name,
                                    commercialName = commercialName.ifBlank { null },
                                    activeIngredient = activeIngredient.ifBlank { null },
                                    categoryName = categoryName,
                                    weightOrSize = weightOrSize.ifBlank { null },
                                    salePrice = sp,
                                    purchasePrice = pp,
                                    imageUrl = null,
                                    description = description.ifBlank { null }
                                )
                            }
                        },
                        enabled = name.isNotBlank(),
                        modifier = Modifier.weight(1.6f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                    ) {
                        Text(if (productToEdit != null) "حفظ التعديلات" else "إضافة الدواء", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
