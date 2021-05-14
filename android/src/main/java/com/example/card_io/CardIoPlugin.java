package com.example.card_io;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.card.payment.CardIOActivity;
import io.card.payment.CardType;
import io.card.payment.CreditCard;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

public class CardIoPlugin implements FlutterPlugin, ActivityAware, PluginRegistry.ActivityResultListener {
  private MethodChannel channel;
  private Activity activity;
  private MethodChannel.Result pendingResult;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
      channel = new MethodChannel(binding.getBinaryMessenger(), "card_io");
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
      channel = null;
  }

  @Override
  public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
      activity = binding.getActivity();
      binding.addActivityResultListener(this);

      channel.setMethodCallHandler(new MethodChannel.MethodCallHandler() {
          @Override
          public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
              if (!call.method.equals("scanCard")) {
                  return;
              }
              pendingResult = result;
              Intent scanIntent = new Intent(activity, CardIOActivity.class);

              boolean requireExpiry = call.hasArgument("requireExpiry") && (requireExpiry = call.argument("requireExpiry"));

              boolean requireCVV = call.hasArgument("requireCVV") && (requireExpiry = call.argument("requireCVV"));

              boolean requirePostalCode = call.hasArgument("requirePostalCode") && (requireExpiry = call.argument("requirePostalCode"));

              boolean requireCardHolderName = call.hasArgument("requireCardHolderName") && (requireExpiry = call.argument("requireCardHolderName"));

              boolean restrictPostalCodeToNumericOnly = call.hasArgument("restrictPostalCodeToNumericOnly") && (requireExpiry = call.argument("restrictPostalCodeToNumericOnly"));

              boolean scanExpiry = true;
              if (call.hasArgument("scanExpiry"))
                  scanExpiry = call.argument("scanExpiry");

              String scanInstructions = null;
              if (call.hasArgument("scanInstructions")) {
                  scanInstructions = call.argument("scanInstructions");
              }

              boolean suppressManualEntry = true;
              if (call.hasArgument("suppressManualEntry"))
                  scanExpiry = call.argument("suppressManualEntry");

              boolean suppressConfirmation = true;
              if (call.hasArgument("suppressConfirmation"))
                  scanExpiry = call.argument("suppressConfirmation");

              boolean hideCardIOLogo = true;
              if (call.hasArgument("hideCardIOLogo"))
                  requireExpiry = call.argument("hideCardIOLogo");
  
              boolean useCardIOLogo = call.hasArgument("useCardIOLogo") && (requireExpiry = call.argument("useCardIOLogo"));

              boolean usePayPalActionbarIcon = false;
              if (call.hasArgument("usePayPalActionbarIcon"))
                  usePayPalActionbarIcon = call.argument("usePayPalActionbarIcon");

              boolean keepApplicationTheme = call.hasArgument("keepApplicationTheme") && (requireExpiry = call.argument("keepApplicationTheme"));

              scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, requireExpiry);
              scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_EXPIRY, scanExpiry);
              scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CVV, requireCVV);
              scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_POSTAL_CODE, requirePostalCode);
              scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_CARDHOLDER_NAME, requireCardHolderName);
              scanIntent.putExtra(CardIOActivity.EXTRA_RESTRICT_POSTAL_CODE_TO_NUMERIC_ONLY, restrictPostalCodeToNumericOnly);
              scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_INSTRUCTIONS, scanInstructions);
              scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, suppressManualEntry);
              scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, suppressConfirmation);
              scanIntent.putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, useCardIOLogo);
              scanIntent.putExtra(CardIOActivity.EXTRA_HIDE_CARDIO_LOGO, hideCardIOLogo);
              scanIntent.putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, usePayPalActionbarIcon);
              scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, keepApplicationTheme);

              activity.startActivityForResult(scanIntent, 100);
          }
      });
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {

  }

  @Override
  public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {

  }

  @Override
  public void onDetachedFromActivity() {
      activity = null;
  }


  @Override
  public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode != 100) {
          return false;
      }
      if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
          CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

          Map<String, Object> response = new HashMap<>();
          response.put("cardholderName", scanResult.cardholderName);
          response.put("cardNumber", scanResult.cardNumber);
          String cardType = null;
          if (scanResult.getCardType() != CardType.UNKNOWN && scanResult.getCardType() != CardType.INSUFFICIENT_DIGITS) {
              switch (scanResult.getCardType()) {
                  case AMEX:
                      cardType = "Amex";
                      break;
                  case DINERSCLUB:
                      cardType = "DinersClub";
                      break;
                  case DISCOVER:
                      cardType = "Discover";
                      break;
                  case JCB:
                      cardType = "JCB";
                      break;
                  case MASTERCARD:
                      cardType = "MasterCard";
                      break;
                  case VISA:
                      cardType = "Visa";
                      break;
                  case MAESTRO:
                      cardType = "Maestro";
                      break;
                  default:
                      break;
              }
          }
          response.put("cardType", cardType);
          response.put("redactedCardNumber", scanResult.getRedactedCardNumber());
          response.put("expiryMonth", scanResult.expiryMonth);
          response.put("expiryYear", scanResult.expiryYear);
          response.put("cvv", scanResult.cvv);
          response.put("postalCode", scanResult.postalCode);
          pendingResult.success(response);
          pendingResult = null;
          return true;
      } else {
          pendingResult.success(null);
          pendingResult = null;
          return false;
      }
  }
}
