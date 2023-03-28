import axios from 'axios';
import buildPaginationQueryOpts from '@/shared/sort/sorts';
import { IUser } from '@/shared/model/user.model';

export default class UserManagementService {
  public get(userId: number): Promise<any> {
    return axios.get(`api/users/${userId}`);
  }

  public create(user: IUser): Promise<any> {
    return axios.post('api/users', user);
  }

  public update(user: IUser): Promise<any> {
    return axios.put('api/users', user);
  }

  public remove(userId: number): Promise<any> {
    return axios.delete(`api/users/${userId}`);
  }

  public retrieve(req?: any): Promise<any> {
    return axios.get(`api/users?${buildPaginationQueryOpts(req)}`);
  }

  public retrieveAuthorities(): Promise<any> {
    return axios.get('api/authorities');
  }

  public retrieveUsersByRole(role: string): Promise<any> {
    return axios.get(`api/users/role/${role}`);
  }
}
