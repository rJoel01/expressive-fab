package com.joel.testing

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.joel.expressive_fab.ExpressiveFabMenu
import com.joel.expressive_fab.ExpressiveFabMenuItem
import com.joel.expressive_fab.extraFunctions.FabMenuExpandDirection
import com.joel.testing.ui.theme.TestingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TestingTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Gray)
                        .padding(vertical = 50.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.Bottom
                ) {

                    var expanded by remember { mutableStateOf(false) }

                    ExpressiveFabMenu(
                        expanded = expanded,
                        button = {
                            IconButton(
                                onClick = {expanded = !expanded},
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.LightGray,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_launcher_foreground),
                                    contentDescription = null
                                )
                            }
                        },
                        expandDirection = FabMenuExpandDirection.ABOVE
                    ) {
                        this.ExpressiveFabMenuItem(
                            onClick = {},
                            text = {
                                Text("Option1")
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )

                        this.ExpressiveFabMenuItem(
                            onClick = {},
                            text = {
                                Text("Option2")
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )

                        this.ExpressiveFabMenuItem(
                            onClick = {},
                            text = {
                                Text("Option3")
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )

                        this.ExpressiveFabMenuItem(
                            onClick = {},
                            text = {
                                Text("Option4")
                            },
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        )
                    }

                }
            }
        }
    }
}
