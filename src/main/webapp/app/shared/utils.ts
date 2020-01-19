import { Dictionary, isArray } from 'lodash';
import { Path } from 'object-path';

export const entriesArr = (obj: Object) =>{

    const ownProps = Object.keys( obj );
    const resArray = new Array(i); // preallocate the Array

    var i = ownProps.length;
    while (i--)
      resArray[i] = [ownProps[i], obj[ownProps[i]]];

    return resArray;
};

const padLeft0 = ( str: string | number, targetLength: number ) => '0'.repeat( targetLength - `${ str }`.length ) + str;
export const makeQueryStringFromObj = ( obj: Dictionary<any> ) => 
    entriesArr( obj )
	.map( ( [ key, val ] ) => `${ encodeURIComponent( key ) }=${ encodeURIComponent( val ) }` )
	.join( '&' );
	
export const formatUtcDate = ( dateTime: Date ) => {
	const date = `${ padLeft0( dateTime.getUTCMonth() + 1, 2 ) }/${ padLeft0( dateTime.getUTCDate(), 2 ) }/${ padLeft0( dateTime.getUTCFullYear(), 4 ) }`;
	const amPm = dateTime.getUTCHours() === 0 || dateTime.getUTCHours() < 12 ? 'am' : 'pm';
	const hour = dateTime.getUTCHours() % 12 ? dateTime.getUTCHours() % 12 : 12;
	const time = `${ padLeft0( hour, 2 ) }:${ padLeft0( dateTime.getUTCMinutes(), 2 ) }${ amPm } UTC`;
	return `${ date } ${ time }`;
};

export const colFieldToStr = ( path: symbol | Path | null ) => {
	if ( path === null ) {
		throw new Error();
	}
	return isArray( path ) ? path.join( '.' ) : String( path );
};

