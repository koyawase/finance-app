import * as yup from "yup";

const phoneNumberPattern = /^\+?[\d\s().-]+$/;

export const customerProfileSchema = yup.object({
  name: yup.string().trim().required("Enter your name."),
  phoneNumber: yup
    .string()
    .trim()
    .required("Enter your phone number.")
    .matches(phoneNumberPattern, {
      message: "Enter a valid phone number.",
      excludeEmptyString: true,
    })
    .test(
      "phone-number-length",
      "Enter a valid phone number.",
      (value) => !value || (value.replace(/\D/g, "").length >= 7 && value.replace(/\D/g, "").length <= 15),
    ),
});
