export class DateUtils {
  public static soon(): Date {
    return DateUtils.relativeDate(7);
  }
  public static recently(): Date {
    return DateUtils.relativeDate(-7);
  }
  public static dateWarn(): Date {
    return DateUtils.relativeDate(35);
  }
  public static dateAlarm(): Date {
    return DateUtils.relativeDate(10);
  }

  public static relativeDate(days: number): Date {
    let now = new Date();
    let then = new Date();
    then.setDate(now.getDate() + days);
    return then;
  }

  public static toLocalDate(dateAsString: string): string {
    if (dateAsString && dateAsString.length > 8) {
      if (dateAsString.startsWith('+1000000000-12-31')) {
        return '';
      }

      const dateObj = new Date(dateAsString);

      if (dateObj > DateUtils.recently() && dateObj < DateUtils.soon()) {
        return dateObj.toLocaleDateString() + ', ' + dateObj.toLocaleTimeString();
      } else {
        return dateObj.toLocaleDateString();
      }
    }

    return '';
  }

  public static getValidToStyle(validToString: string): string {
    const now = new Date();
    const validTo = new Date(validToString);

    if (validTo > now && validTo < DateUtils.dateAlarm()) {
      return 'color:red;font-weight: bold;';
    } else if (validTo > now && validTo < DateUtils.dateWarn()) {
      return 'color:olive; font-weight: bold;';
    } else if (validTo > now) {
      return 'color:green; font-weight: bold;';
    }
    return '';
  }

  public static getCertValidToStyle(validFromString: string, validToString: string, revoked: boolean): string {
    if (revoked) {
      return '';
    }

    const now = new Date();
    const validTo = new Date(validToString);
    const validFrom = new Date(validFromString);

    if (validTo > now && validTo < DateUtils.dateAlarm()) {
      return 'color:red;font-weight: bold;';
    } else if (validTo > now && validTo < DateUtils.dateWarn()) {
      return 'color:olive; font-weight: bold;';
    } else if (validTo > now && validFrom <= now) {
      return 'color:green; font-weight: bold;';
    }
    return '';
  }
}
