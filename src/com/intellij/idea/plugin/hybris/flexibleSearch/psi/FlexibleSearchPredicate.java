// This is a generated file. Not intended for manual editing.
package com.intellij.idea.plugin.hybris.flexibleSearch.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface FlexibleSearchPredicate extends PsiElement {

  @Nullable
  FlexibleSearchBetweenPredicate getBetweenPredicate();

  @Nullable
  FlexibleSearchComparisonPredicate getComparisonPredicate();

  @Nullable
  FlexibleSearchExistsPredicate getExistsPredicate();

  @Nullable
  FlexibleSearchLikePredicate getLikePredicate();

  @Nullable
  FlexibleSearchNullPredicate getNullPredicate();

}
