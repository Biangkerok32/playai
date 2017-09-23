package com.jimx.playapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.StringTokenizer;

import gnu.mapping.Location;

/**
 * Created by jimx on 17-4-23.
 */
/* Translated from appinventor/blocklyeditor/src/generators/yail.js */
public class YailGenerator {

    private static final String TAG = "YailGenerator";

    private static final String YAIL_ADD_COMPONENT = "(add-component ";
    private static final String YAIL_ADD_TO_LIST = "(add-to-list ";
    private static final String YAIL_BEGIN = "(begin ";
    private static final String YAIL_CALL_COMPONENT_METHOD = "(call-component-method ";
    private static final String YAIL_CALL_COMPONENT_TYPE_METHOD = "(call-component-type-method ";
    private static final String YAIL_CALL_YAIL_PRIMITIVE = "(call-yail-primitive ";
    private static final String YAIL_CLEAR_FORM = "(clear-current-form)";
    // The lines below are complicated because we want to support versions of the
    // Companion older then 2.20ai2 which do not have set-form-name defined
    private static final String YAIL_SET_FORM_NAME_BEGIN = "(try-catch (let ((attempt (delay (set-form-name \"";
    private static final String YAIL_SET_FORM_NAME_END = "\")))) (force attempt)) (exception java.lang.Throwable 'notfound))";
    private static final String YAIL_CLOSE_COMBINATION = ")";
    private static final String YAIL_CLOSE_BLOCK = ")\n";
    private static final String YAIL_COMMENT_MAJOR = ";;; ";
    private static final String YAIL_COMPONENT_REMOVE = "(remove-component ";
    private static final String YAIL_COMPONENT_TYPE = "component";
    private static final String YAIL_DEFINE = "(def ";
    private static final String YAIL_DEFINE_EVENT = "(define-event ";
    private static final String YAIL_DEFINE_FORM = "(define-form ";
    private static final String YAIL_DO_AFTER_FORM_CREATION = "(do-after-form-creation ";
    private static final String YAIL_DOUBLE_QUOTE = "\"";
    private static final String YAIL_FALSE = "#f";
    private static final String YAIL_FOREACH = "(foreach ";
    private static final String YAIL_FORRANGE = "(forrange ";
    private static final String YAIL_GET_COMPONENT = "(get-component ";
    private static final String YAIL_GET_PROPERTY = "(get-property ";
    private static final String YAIL_GET_COMPONENT_TYPE_PROPERTY = "(get-property-and-check  ";
    private static final String YAIL_GET_VARIABLE = "(get-var ";
    private static final String YAIL_AND_DELAYED = "(and-delayed ";
    private static final String YAIL_OR_DELAYED = "(or-delayed ";
    private static final String YAIL_IF = "(if ";
    private static final String YAIL_INIT_RUNTIME = "(init-runtime)";
    private static final String YAIL_INITIALIZE_COMPONENTS = "(call-Initialize-of-components";
    private static final String YAIL_LET = "(let ";
    private static final String YAIL_LEXICAL_VALUE = "(lexical-value ";
    private static final String YAIL_SET_LEXICAL_VALUE = "(set-lexical! ";
    private static final String YAIL_LINE_FEED = "\n";
    private static final String YAIL_NULL = "(get-var *the-null-value*)";
    private static final String YAIL_EMPTY_LIST = "'()";
    private static final String YAIL_OPEN_BLOCK = "(";
    private static final String YAIL_OPEN_COMBINATION = "(";
    private static final String YAIL_QUOTE = "'";
    private static final String YAIL_RENAME_COMPONENT = "(rename-component ";
    private static final String YAIL_SET_AND_COERCE_PROPERTY = "(set-and-coerce-property! ";
    private static final String YAIL_SET_AND_COERCE_COMPONENT_TYPE_PROPERTY = "(set-and-coerce-property-and-check! ";
    private static final String YAIL_SET_SUBFORM_LAYOUT_PROPERTY = "(%set-subform-layout-property! ";
    private static final String YAIL_SET_VARIABLE = "(set-var! ";
    private static final String YAIL_SET_THIS_FORM = "(set-this-form)\n ";
    private static final String YAIL_SPACER = " ";
    private static final String YAIL_TRUE = "#t";
    private static final String YAIL_WHILE = "(while ";
    private static final String YAIL_LIST_CONSTRUCTOR = "*list-for-runtime*";


    public static String getComponentLines(String formName, JSONObject componentJson, String parentName) throws JSONException {
        if (componentJson.getString("$Type").equals("Form")) {
            StringBuffer initializer = new StringBuffer();
            StringBuffer sb = new StringBuffer();

            initializer.append(YAIL_INITIALIZE_COMPONENTS);
            initializer.append(YAIL_SPACER + YAIL_QUOTE + formName);
            sb.append(getFormPropertiesLines(formName, componentJson));

            JSONArray components = componentJson.getJSONArray("$Components");
            for (int i = 0; i < components.length(); i++) {
                initializer.append(YAIL_SPACER + YAIL_QUOTE + components.getJSONObject(i).getString("$Name"));
                sb.append(getComponentLines(formName, components.getJSONObject(i), formName));
            }
            initializer.append(YAIL_CLOSE_BLOCK);
            return sb.toString() + initializer.toString();
        }
        return getComponentPropertiesLines(formName, componentJson, parentName);
    }

    public static String getComponentPropertiesLines(String formName, JSONObject componentJson,
                                                String parentName) throws JSONException {
        StringBuffer sb = new StringBuffer();
        String componentName = componentJson.getString("$Name");
        String componentType = componentJson.getString("$Type");

        sb.append(YAIL_ADD_COMPONENT + parentName + YAIL_SPACER +
                "com.google.appinventor.components.runtime." + componentType +
                YAIL_SPACER + componentName + YAIL_SPACER);
        sb.append(getPropertySettersLines(componentJson, componentName));
        sb.append(YAIL_CLOSE_BLOCK);
        return sb.toString();
    }

    public static String getFormPropertiesLines(String formName, JSONObject componentJson) throws JSONException {
        String propLines = getPropertySettersLines(componentJson, formName);

        return YAIL_DO_AFTER_FORM_CREATION + propLines +
                YAIL_CLOSE_BLOCK;
    }

    public static String getPropertySettersLines(JSONObject componentJson, String componentName) throws JSONException {
        StringBuffer sb = new StringBuffer();

        Iterator<String> keys = componentJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();

            if (!key.startsWith("$") && !key.equals("Uuid")) {
                Object value = componentJson.get(key);
                Log.i(TAG, key + value.toString());
                sb.append(getPropertySetterString(componentName, componentJson.getString("$Type"),
                        key, value));
            }
        }
        return sb.toString();
    }

    public static String getPropertySetterString(String componentName, String componentType,
                                                 String propertyName, Object propertyValue) throws JSONException {
        StringBuffer sb = new StringBuffer();
        sb.append(YAIL_SET_AND_COERCE_PROPERTY + YAIL_QUOTE +
                componentName + YAIL_SPACER + YAIL_QUOTE + propertyName +
                YAIL_SPACER);

        /* Determine the real type of the property */
        String qualType = "com.google.appinventor.components.runtime." + componentType;
        try {
            Log.i(TAG, qualType + " " + propertyName);
            Class<?> propClass = Class.forName(qualType);

            Method method = null;
            while (method == null && propClass != null) {
                Method[] methods = propClass.getDeclaredMethods();
                int i;
                for (i = 0; i < methods.length; i++) {
                    if (methods[i].getName().equals(propertyName) && methods[i].getParameterTypes().length == 1) {
                        method = methods[i];
                        break;
                    }
                }
                propClass = propClass.getSuperclass();
            }

            if (method == null) return "";
            String javaPropType = method.getParameterTypes()[0].getName();
            Log.i(TAG, qualType + " " + propertyName + " " + javaPropType);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return "";
        }

        String propType = YAIL_QUOTE + "text";
        String value = "\"" + propertyValue.toString() + "\"";
        sb.append(value + YAIL_SPACER + propType + YAIL_CLOSE_BLOCK);
        return sb.toString();
    }
}
